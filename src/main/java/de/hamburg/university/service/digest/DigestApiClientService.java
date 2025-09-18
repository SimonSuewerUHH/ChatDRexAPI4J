package de.hamburg.university.service.digest;

import de.hamburg.university.agent.tool.ToolFileResponseDTO;
import de.hamburg.university.agent.tool.ToolFileResponseType;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DigestApiClientService {

    private static final Logger LOG = Logger.getLogger(DigestApiClientService.class);

    @Inject
    @RestClient
    DigestApiClient digestApiClient;

    @ConfigProperty(name = "quarkus.rest-client.digest-client.url")
    String digestClientUrl;

    @Inject
    protected Vertx vertx;

    @Inject
    DigestFormatterService formatterService;

    protected long pollIntervalMillis() {
        return 1000L;
    }

    public Uni<DigestToolResultDTO> callSubnetwork(List<String> target) {
        LOG.infof("Submitting subnetwork request with %d targets", target.size());
        DigestSubmitRequestDTO request = DigestSubmitRequestDTO.forSubnetwork(target);

        return digestApiClient.submitSubnetwork(request)
                .onItem().invoke(r -> LOG.debugf("Received subnetwork task response: %s", r))
                .onItem().transform(DigestTaskResponseDTO::getTask)
                .onItem().transformToUni(uid -> {
                    LOG.infof("Polling results for subnetwork taskId=%s", uid);
                    return retrieveResults(uid);
                });
    }

    public Uni<DigestToolResultDTO> callSet(List<String> target) {
        LOG.infof("Submitting set request with %d targets", target.size());
        DigestSubmitRequestDTO request = DigestSubmitRequestDTO.forSubnetwork(target);

        return digestApiClient.submitSet(request)
                .onItem().invoke(r -> LOG.debugf("Received set task response: %s", r))
                .onItem().transform(DigestTaskResponseDTO::getTask)
                .onItem().transformToUni(uid -> {
                    LOG.infof("Polling results for set taskId=%s", uid);
                    return retrieveResults(uid);
                });
    }

    @Timeout(value = 180, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "fallback")
    public Uni<DigestToolResultDTO> retrieveResults(String uid) {
        return Uni.createFrom().emitter(emitter -> {
            LOG.infof("Starting result polling for taskId=%s", uid);

            long timerId = vertx.setPeriodic(pollIntervalMillis(), id -> {
                digestApiClient.status(uid).whenComplete((statusResponse, throwable) -> {
                    if (throwable != null) {
                        LOG.errorf(throwable, "Error while retrieving status for taskId=%s", uid);
                        emitter.fail(throwable);
                        vertx.cancelTimer(id);
                        return;
                    }

                    if (statusResponse != null) {
                        LOG.debugf("Status for taskId=%s -> done=%s, failed=%s, status=%s",
                                uid, statusResponse.getDone(), statusResponse.getFailed(), statusResponse.getStatus());

                        if (statusResponse.getDone()) {
                            LOG.infof("TaskId=%s completed successfully", uid);
                            vertx.cancelTimer(id);

                            digestApiClient.result(uid)
                                    .onItem().invoke(r -> LOG.debugf("Fetched result for taskId=%s", uid))
                                    .onFailure().invoke(e -> LOG.errorf(e, "Failed to fetch result for taskId=%s", uid))
                                    .subscribe().with(
                                            r -> {
                                                //TODO CHANGE TO NON BLOCKING
                                                vertx.<DigestToolResultDTO>executeBlocking(promise -> {
                                                    try {
                                                        DigestToolResultDTO formatted =
                                                                formatterService.formatDigestOutputStructured(r.getResult(), uid); // blocking OK here
                                                        promise.complete(formatted);
                                                    } catch (Throwable t) {
                                                        promise.fail(t);
                                                    }
                                                }, false, ar -> {
                                                    if (ar.succeeded()) {
                                                        emitter.complete(ar.result());
                                                        LOG.infof("Emitted formatted result for taskId=%s", uid);
                                                    } else {
                                                        emitter.fail(ar.cause());
                                                        LOG.errorf(ar.cause(), "Failed to format result for taskId=%s", uid);
                                                    }
                                                });
                                            },
                                            emitter::fail
                                    );
                        }

                        if (statusResponse.getFailed()) {
                            LOG.warnf("TaskId=%s failed with status=%s", uid, statusResponse.getStatus());
                            emitter.fail(new Throwable(statusResponse.getStatus()));
                            vertx.cancelTimer(id);
                        }
                    }
                });
            });

            emitter.onTermination(() -> {
                LOG.infof("Terminating polling for taskId=%s (timerId=%s)", uid, timerId);
                vertx.cancelTimer(timerId);
            });
        });
    }

    public DigestToolPlotDTO createPlot(DigestToolResultDTO result) {
        List<DigestToolPlotEntryDTO> entries = new ArrayList<>();

        if (result != null && result.getRows() != null) {
            for (DigestToolResultDTO.Row row : result.getRows()) {
                List<String> genes = row.getGene() != null ? row.getGene() : List.of();
                List<String> descriptions = row.getDescription() != null ? row.getDescription() : List.of();

                String desc = descriptions.isEmpty() ? "" : String.join("; ", descriptions);

                if (row.getDbTerms() != null) {
                    for (Map.Entry<String, Integer> termEntry : row.getDbTerms().entrySet()) {
                        String term = termEntry.getKey();
                        Integer score = termEntry.getValue();

                        if (!genes.isEmpty()) {
                            for (String gene : genes) {
                                entries.add(new DigestToolPlotEntryDTO(
                                        row.getDatabase(),
                                        term,
                                        score,
                                        row.getEmpiricalPValue(),
                                        desc,
                                        gene
                                ));
                            }
                        } else {
                            entries.add(new DigestToolPlotEntryDTO(
                                    row.getDatabase(),
                                    term,
                                    score,
                                    row.getEmpiricalPValue(),
                                    desc,
                                    null
                            ));
                        }
                    }
                }
            }
        }

        return new DigestToolPlotDTO(entries);
    }

    public Uni<DigestToolResultDTO> fallback(String uid) {
        return Uni.createFrom().failure(new RuntimeException("Operation timed out after 60 seconds (uid=" + uid + ")."));
    }

    public List<ToolFileResponseDTO> getFileList(String uid) {
        return digestApiClient.resultFileList(uid).stream().map(
                r -> {
                    String path = digestClientUrl + "/result_file?name=" + r.getName();
                    String name = r.getName()
                            .replaceAll(uid, "")
                            .replaceAll("_", "")
                            .split("\\.")[0];
                    String typeString = r.getType();

                    ToolFileResponseType type = switch (typeString) {
                        case "png" -> ToolFileResponseType.PNG;
                        case "csv" -> ToolFileResponseType.CSV;
                        default -> ToolFileResponseType.DOWNLOAD;
                    };
                    return new ToolFileResponseDTO(path, name, type);
                }).toList();
    }
}