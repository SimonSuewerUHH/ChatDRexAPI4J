package de.hamburg.university.service.digest;

import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class DigestApiClientService {

    @Inject
    @RestClient
    DigestApiClient digestApiClient;

    @Inject
    protected Vertx vertx;

    @Inject
    DigestFormatterService formatterService;

    protected long pollIntervalMillis() {
        return 1000L;
    }

    protected int timeoutSeconds() {
        return 30;
    }


    public Uni<String> callSubnetwork(List<String> target) {
        DigestSubmitRequestDTO request = DigestSubmitRequestDTO.forSubnetwork(target);
        DigestTaskResponseDTO response = digestApiClient.submitSubnetwork(request);
        return retrieveResults(response.getTask());
    }

    public Uni<String> callSet(List<String> target) {
        DigestSubmitRequestDTO request = DigestSubmitRequestDTO.forSubnetwork(target);
        DigestTaskResponseDTO response = digestApiClient.submitSet(request);
        return retrieveResults(response.getTask());
    }

    public Uni<String> retrieveResults(String uid) {
        return Uni.createFrom().emitter(emitter -> {
            long timerId = vertx.setPeriodic(pollIntervalMillis(), id -> {
                digestApiClient.status(uid).whenComplete((statusResponse, throwable) -> {
                    if (throwable != null) {
                        emitter.fail(throwable);
                        vertx.cancelTimer(id);
                        return;
                    }
                    if (statusResponse != null && statusResponse.getDone()) {
                        vertx.cancelTimer(id);
                        DigestResultResponseDTO result = digestApiClient.result(uid);
                        String formatted = formatterService.formatDigestOutput(result.getResult());
                        emitter.complete(formatted);

                    }
                    if (statusResponse != null && statusResponse.getFailed()) {
                        emitter.fail(new Throwable(statusResponse.getStatus()));
                        vertx.cancelTimer(id);

                    }
                });
            });
            emitter.onTermination(() -> vertx.cancelTimer(timerId));
        });
    }
}
