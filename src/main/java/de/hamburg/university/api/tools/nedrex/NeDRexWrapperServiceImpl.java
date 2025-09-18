package de.hamburg.university.api.tools.nedrex;

import de.hamburg.university.service.nedrex.NeDRexAPIInfoDTO;
import de.hamburg.university.service.nedrex.NeDRexService;
import de.hamburg.university.service.nedrex.diamond.DiamondResultsDTO;
import de.hamburg.university.service.nedrex.diamond.DiamondToolClientService;
import de.hamburg.university.service.nedrex.diamond.SeedPayloadDTO;
import de.hamburg.university.service.nedrex.trustrank.TrustRankResultDTO;
import de.hamburg.university.service.nedrex.trustrank.TrustRankSeedPayloadDTO;
import de.hamburg.university.service.nedrex.trustrank.TrustRankToolClientService;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;

public class NeDRexWrapperServiceImpl implements NeDRexWrapperService {


    @Inject
    DiamondToolClientService diamondTool;

    @Inject
    TrustRankToolClientService trustRankTool;

    @Inject
    NeDRexService neDRexService;

    @Override
    public NeDRexAPIInfoDTO query(String query) {
        return neDRexService.fetchSingleInfo(query);
    }

    @Override
    public Uni<DiamondResultsDTO> runDiamond(SeedPayloadDTO payload) {
        return diamondTool.run(payload)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onFailure().invoke(t -> Log.error("DIAMOnD run failed", t));

    }

    @Override
    public Uni<TrustRankResultDTO> runTrustRank(TrustRankSeedPayloadDTO payload) {
        return trustRankTool.run(payload)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onFailure().invoke(t -> Log.error("TrustRank run failed", t));
    }
}
