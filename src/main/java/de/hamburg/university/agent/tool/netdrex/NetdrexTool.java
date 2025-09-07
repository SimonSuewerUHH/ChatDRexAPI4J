package de.hamburg.university.agent.tool.netdrex;

import de.hamburg.university.agent.bot.NetDrexToolDecisionBot;
import de.hamburg.university.agent.planning.PlanState;
import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import de.hamburg.university.helper.drugstone.DrugstOneGraphHelper;
import de.hamburg.university.helper.drugstone.DrugstOneNetworkDTO;
import de.hamburg.university.service.netdrex.diamond.DiamondResultsDTO;
import de.hamburg.university.service.netdrex.diamond.DiamondToolClientService;
import de.hamburg.university.service.netdrex.diamond.SeedPayloadDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankResultDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankSeedPayloadDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankToolClientService;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.subscription.MultiEmitter;
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

    @Inject
    DrugstOneGraphHelper drugstOneGraphHelper;

    @Inject
    ChatWebsocketSender chatWebsocketSender;

    public PlanState answer(PlanState state, ChatRequestDTO content, MultiEmitter<? super ChatResponseDTO> emitter) {
        ToolDTO toolDTO = new ToolDTO(Tools.NETDREX_TOOL.name());
        NetdrexToolDecisionResult result = netDrexToolDecisionBot.answer(state.getUserGoal(), state.getEnhancedQueryBioInfo());
        toolDTO.setInput(result.getToolName() + " with " + result.getEntrezIds().size() + " entrezIds");

        chatWebsocketSender.sendTool(toolDTO, content, emitter);
        if (result.getEntrezIds().isEmpty()) {
            Log.errorf("No enhanced query bio info found for user %s", state.getUserGoal());
            toolDTO.setStop();
            toolDTO.addContent("No entrezIds found");
            chatWebsocketSender.sendTool(toolDTO, content, emitter);
            return state;
        }
        if (result.getToolName().equalsIgnoreCase("diamond")) {
            Uni<DiamondResultsDTO> diamondResult = runDiamond(result.getEntrezIds());
            DrugstOneNetworkDTO network = drugstOneGraphHelper.diamondToNetwork(diamondResult.await().indefinitely());
            state.setDrugstOneNetwork(network);
            toolDTO.addStructuredContent(network);
        } else if (result.getToolName().equalsIgnoreCase("trustrank")) {
            Uni<TrustRankResultDTO> trustRankResult = runTrustrank(result.getEntrezIds());
           // TrustRankResultDTO result = trustRankResult.await().indefinitely();
            //state.setTrustRankResult(trustRankResult.await().indefinitely());
            //toolDTO.addStructuredContent(state.getTrustRankResult());
        }
        toolDTO.addContent("Tool " + result.getToolName() + " executed with " + result.getEntrezIds().size() + " entrezIds");
        toolDTO.setStop();
        chatWebsocketSender.sendTool(toolDTO, content, emitter);
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
