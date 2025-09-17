package de.hamburg.university.service.netdrex;

import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

public abstract class BaseNedrexTool<A extends NetdrexJobApi<P, S>, P, S extends NetdrexStatusResponseDTO<T>, T, R> {

    @Inject
    protected Vertx vertx;

    @Inject
    @RestClient
    protected A api;

    protected abstract R mapResult(T result);


    protected long timeoutMSeconds() {
        return 30 * 1000L;
    }

    protected String sanitizeUid(String raw) {
        return raw == null ? null : raw.replace("\"", "").trim();
    }

    public Uni<String> submit(P payload) {
        Objects.requireNonNull(payload, "payload");
        return api.submit(payload)
                .onItem().transform(this::sanitizeUid)
                .onItem().invoke(uid -> {
                    if (uid == null || uid.isBlank()) {
                        throw new RuntimeException("No UID returned from submit().");
                    }
                });
    }
    public Uni<R> fallback(String uid) {
        return Uni.createFrom().failure(new RuntimeException("Operation timed out after 60 seconds (uid=" + uid + ")."));
    }

    @Timeout(value = 60, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "fallback")
    public Uni<R> retrieveResults(String uid) {
        Objects.requireNonNull(uid, "uid");
        return Uni.createFrom().emitter(emitter -> {

            long pollTimerId = vertx.setPeriodic(timeoutMSeconds(), id -> {
                api.status(uid).whenComplete((statusResponse, throwable) -> {
                    if (throwable != null) {

                        vertx.cancelTimer(id);
                        emitter.fail(new RuntimeException("Polling aborted after consecutive failures (uid=" + uid + "). Last error: " + throwable.getMessage(), throwable));
                        return;
                    }


                    if (statusResponse == null || statusResponse.getStatus() == null) {
                        // Nothing usable yet; continue polling
                        return;
                    }

                    switch (statusResponse.getStatus()) {
                        case COMPLETED -> {
                            T result = statusResponse.getResults();
                            R resultsDTO = mapResult(result);
                            vertx.cancelTimer(id);
                            emitter.complete(resultsDTO);
                        }
                        case FAILED -> {
                            vertx.cancelTimer(id);
                            String msg = "Remote job " + statusResponse.getStatus() + " (uid=" + uid + ").";
                            emitter.fail(new RuntimeException(msg));
                        }
                        default -> {
                            // PENDING/RUNNING etc.: keep polling
                        }
                    }
                });
            });

            // Ensure timers are cleaned up if the Uni is cancelled/terminated
            emitter.onTermination(() -> {
                vertx.cancelTimer(pollTimerId);
            });
        });
    }

    public Uni<R> run(P payload) {
        return submit(payload)
                .flatMap(this::retrieveResults);
    }
}
