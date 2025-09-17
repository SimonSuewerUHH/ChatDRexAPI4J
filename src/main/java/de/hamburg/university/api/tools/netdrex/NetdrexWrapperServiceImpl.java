package de.hamburg.university.api.tools.netdrex;

import de.hamburg.university.service.netdrex.NetdrexAPIInfoDTO;
import de.hamburg.university.service.netdrex.NetdrexService;
import de.hamburg.university.service.netdrex.diamond.DiamondResultsDTO;
import de.hamburg.university.service.netdrex.diamond.DiamondToolClientService;
import de.hamburg.university.service.netdrex.diamond.SeedPayloadDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankResultDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankSeedPayloadDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankToolClientService;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;

public class NetdrexWrapperServiceImpl implements NetdrexWrapperService {


    @Inject
    DiamondToolClientService diamondTool;

    @Inject
    TrustRankToolClientService trustRankTool;

    @Inject
    NetdrexService netdrexService;

    @Override
    public NetdrexAPIInfoDTO query(String query) {
        return netdrexService.fetchSingleInfo(query);
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
