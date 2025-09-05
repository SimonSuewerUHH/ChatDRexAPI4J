package de.hamburg.university.service.netdrex;

import io.smallrye.mutiny.Uni;

import java.util.concurrent.CompletionStage;

public interface NetdrexJobApi<P, R extends NetdrexStatusResponseDTO<?>> {
    Uni<String> submit(P payload);

    CompletionStage<R> status(String uid);
}
