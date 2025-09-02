package de.hamburg.university.service.netdrex;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Objects;

public abstract class BaseNedrexTool<A extends NetdrexJobApi<P, S>, P, S extends NetdrexStatusResponseDTO<T>, T, R> {

    @Inject
    protected Vertx vertx;

    @Inject
    @RestClient
    protected A api;

    protected abstract R mapResult(T result);


    protected long pollIntervalMillis() {
        return 1000L;
    }

    protected int timeoutSeconds() {
        return 30;
    }


    protected String sanitizeUid(String raw) {
        return raw == null ? null : raw.replace("\"", "").trim();
    }


    public String submit(P payload) {
        Objects.requireNonNull(payload, "payload");
        try {
            String rawUid = api.submit(payload);
            String uid = sanitizeUid(rawUid);
            if (uid == null || uid.isBlank()) {
                throw new RuntimeException("No UID returned from submit().");
            }
            return uid;
        } catch (Exception e) {
            Log.error("Submit failed", e);
            throw new RuntimeException("Submit failed: " + e.getMessage(), e);
        }
    }

    //@Timeout(value = 30, unit = ChronoUnit.SECONDS)
    public Uni<R> retrieveResults(String uid) {
        return Uni.createFrom().emitter(emitter -> {
            long timerId = vertx.setPeriodic(pollIntervalMillis(), id -> {
                api.status(uid).whenComplete((statusResponse, throwable) -> {
                    if (throwable != null) {
                        emitter.fail(throwable);
                        vertx.cancelTimer(id);
                        return;
                    }
                    if (statusResponse != null && NetdrexStatus.COMPLETED == statusResponse.getStatus()) {
                        T result = statusResponse.getResults();
                        R resultsDTO = mapResult(result);
                        vertx.cancelTimer(id);
                        emitter.complete(resultsDTO);

                    }
                });
            });
            emitter.onTermination(() -> vertx.cancelTimer(timerId));
        });
    }


    public Uni<R> run(P payload) {
        String uid = submit(payload);
        return retrieveResults(uid);
    }
}
