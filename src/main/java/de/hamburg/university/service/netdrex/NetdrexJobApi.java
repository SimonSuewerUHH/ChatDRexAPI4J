package de.hamburg.university.service.netdrex;

import java.util.concurrent.CompletionStage;

public interface NetdrexJobApi<P, R extends NetdrexStatusResponseDTO<?>> {
    String submit(P payload);

    CompletionStage<R> status(String uid);
}
