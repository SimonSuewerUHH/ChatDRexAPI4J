package de.hamburg.university.api.tools.digest;

import de.hamburg.university.service.digest.DigestApiClientService;
import de.hamburg.university.service.digest.DigestToolResultDTO;
import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DigestWrapperServiceImpl implements DigestWrapperService {


    @Inject
    DigestApiClientService digestService;

    @Override
    @Blocking
    public Uni<DigestToolResultDTO> submitSet(DigestTargetsRequestDTO body) {
        return digestService.callSet(body.getTarget())
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onFailure().invoke(t -> Log.error("DigestSet run failed", t));
    }

    @Override
    public Uni<DigestToolResultDTO> submitSubnetwork(DigestTargetsRequestDTO body) {
        return digestService.callSubnetwork(body.getTarget())
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onFailure().invoke(t -> Log.error("DigestSubnetwork run failed", t));
    }
}