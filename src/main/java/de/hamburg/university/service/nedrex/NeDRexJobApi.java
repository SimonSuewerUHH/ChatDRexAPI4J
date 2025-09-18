package de.hamburg.university.service.nedrex;

import io.smallrye.mutiny.Uni;

import java.util.concurrent.CompletionStage;

public interface NeDRexJobApi<P, R extends NeDRexStatusResponseDTO<?>> {
    Uni<String> submit(P payload);

    CompletionStage<R> status(String uid);
}
