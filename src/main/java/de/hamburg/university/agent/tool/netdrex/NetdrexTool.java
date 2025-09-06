package de.hamburg.university.agent.tool.netdrex;

import de.hamburg.university.agent.bot.NetDrexToolDecisionBot;
import de.hamburg.university.agent.planning.PlanState;
import de.hamburg.university.service.netdrex.diamond.DiamondResultsDTO;
import de.hamburg.university.service.netdrex.diamond.DiamondToolClientService;
import de.hamburg.university.service.netdrex.diamond.SeedPayloadDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankResultDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankSeedPayloadDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankToolClientService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

import static io.quarkus.arc.ComponentsProvider.LOG;

@ApplicationScoped
public class NetdrexTool {

    @Inject
    DiamondToolClientService diamondToolService;

    @Inject
    TrustRankToolClientService trustRankToolService;

    @Inject
    NetDrexToolDecisionBot netDrexToolDecisionBot;

    public PlanState answer(PlanState state) {
        NetdrexToolDecisionResult result = netDrexToolDecisionBot.answer(state.getUserGoal(), state.getEnhancedQueryBioInfo());

        if (result.getToolName().equalsIgnoreCase("diamond")) {
            Uni<DiamondResultsDTO> diamondResult = runDiamond(result.getEntrezIds());
            state.setDiamondResult(diamondResult.await().indefinitely());
        } else if (result.getToolName().equalsIgnoreCase("trustrank")) {
            Uni<TrustRankResultDTO> trustRankResult = runTrustrank(result.getEntrezIds());
            state.setTrustRankResult(trustRankResult.await().indefinitely());
        }
        return state;
    }

    public Uni<DiamondResultsDTO> runDiamond(List<String> entrezIds) {
        return Uni.createFrom().item(() -> entrezIds.stream()
                        .map(String::valueOf)
                        .toList()
                )
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .flatMap(ids -> diamondToolService.run(new SeedPayloadDTO(ids)))
                .onFailure().invoke(e -> LOG.error("Error at Diamond-Tool", e))
                .onFailure().transform(e -> new RuntimeException("Error at Diamond-Tool: " + e.getMessage(), e));
    }

    public Uni<TrustRankResultDTO> runTrustrank(List<String> entrezIds) {
        TrustRankSeedPayloadDTO payload = new TrustRankSeedPayloadDTO();
        payload.setN(10);
        payload.setDampingFactor(0.85);
        payload.setOnlyApprovedDrugs(false);
        payload.setOnlyDirectDrugs(false);
        return Uni.createFrom().item(() -> entrezIds.stream()
                        .map(String::valueOf)
                        .toList()
                )
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .flatMap(ids -> {
                    payload.setSeeds(ids);
                    return trustRankToolService.run(payload);
                })
                .onFailure().invoke(e -> LOG.error("Error at Trustrank-Tool", e))
                .onFailure().transform(e -> new RuntimeException("Error at Truntrank-Tool: " + e.getMessage(), e));
    }

}
