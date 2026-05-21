package de.hamburg.university.agent.tool.netdrex;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.agent.bot.NetDrexToolDecisionBot;
import de.hamburg.university.agent.planning.PlanState;
import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import de.hamburg.university.helper.drugstone.DrugstOneGraphHelper;
import de.hamburg.university.helper.drugstone.DrugstOneNetworkDTO;
import de.hamburg.university.service.netdrex.closeness.ClosenessResultDTO;
import de.hamburg.university.service.netdrex.closeness.ClosenessSeedPayloadDTO;
import de.hamburg.university.service.netdrex.closeness.ClosenessToolClientService;
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
    ClosenessToolClientService closenessToolClientService;

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
        if (result.getEntrezIds().isEmpty()) {
            Log.errorf("No enhanced query bio info found for user %s", state.getUserGoal());
            toolDTO.setStop();
            toolDTO.addContent("No entrezIds found");
            chatWebsocketSender.sendTool(toolDTO, content, emitter);
            return state;
        }
        if (result.getToolName().equalsIgnoreCase("diamond")) {
            SeedPayloadDTO payload = getDiamondPayload(result.getEntrezIds());
            toolDTO.addContent("Run with:" + getJson(payload));
            chatWebsocketSender.sendTool(toolDTO, content, emitter);

            Uni<DiamondResultsDTO> diamondResult = runDiamond(payload);
            DrugstOneNetworkDTO network = drugstOneGraphHelper.diamondToNetwork(diamondResult.await().indefinitely());
            state.setDrugstOneNetwork(network);
            toolDTO.addStructuredContent(network);
        } else if (result.getToolName().equalsIgnoreCase("trustrank")) {
            TrustRankSeedPayloadDTO payload = getTrustrankPayload(result.getEntrezIds());
            toolDTO.addContent("Run with:" + getJson(payload));
            chatWebsocketSender.sendTool(toolDTO, content, emitter);

            Uni<TrustRankResultDTO> trustRankResult = runTrustrank(payload);
            DrugstOneNetworkDTO network = drugstOneGraphHelper.trustrankToNetwork(trustRankResult.await().indefinitely());
            state.setDrugstOneNetwork(network);
            toolDTO.addStructuredContent(network);
        } else if (result.getToolName().equalsIgnoreCase("closeness")) {
            ClosenessSeedPayloadDTO payload = getClosenessPayload(result.getEntrezIds());
            toolDTO.addContent("Run with:" + getJson(payload));
            chatWebsocketSender.sendTool(toolDTO, content, emitter);

            Uni<ClosenessResultDTO> closenessResult = runCloseness(payload);
            DrugstOneNetworkDTO network = drugstOneGraphHelper.trustrankToNetwork(closenessResult.await().indefinitely());
            state.setDrugstOneNetwork(network);
            toolDTO.addStructuredContent(network);
        }


        toolDTO.addContent("Tool " + result.getToolName() + " executed with " + result.getEntrezIds().size() + " entrezIds");
        toolDTO.setStop();
        chatWebsocketSender.sendTool(toolDTO, content, emitter);
        return state;
    }


    public Uni<DiamondResultsDTO> runDiamond(SeedPayloadDTO seedPayloadDTO) {
        return Uni.createFrom().item(() -> seedPayloadDTO
                )
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .flatMap(payload -> diamondToolService.run(payload))
                .onFailure().invoke(e -> LOG.error("Error at Diamond-Tool", e))
                .onFailure().transform(e -> new RuntimeException("Error at Diamond-Tool: " + e.getMessage(), e));
    }

    public Uni<TrustRankResultDTO> runTrustrank(TrustRankSeedPayloadDTO payload) {
        return Uni.createFrom().item(() -> payload)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .flatMap(p -> trustRankToolService.run(p))
                .onFailure().invoke(e -> LOG.error("Error at Trustrank-Tool", e))
                .onFailure().transform(e -> new RuntimeException("Error at Truntrank-Tool: " + e.getMessage(), e));
    }

    public Uni<ClosenessResultDTO> runCloseness(ClosenessSeedPayloadDTO payload) {

        return Uni.createFrom().item(() -> payload
                )
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .flatMap(p -> closenessToolClientService.run(p))
                .onFailure().invoke(e -> LOG.error("Error at Closeness-Tool", e))
                .onFailure().transform(e -> new RuntimeException("Error at Closeness-Tool: " + e.getMessage(), e));
    }

    private String getJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            return "{}";
        }
    }


    private SeedPayloadDTO getDiamondPayload(List<String> entrezIds) {
        return new SeedPayloadDTO(entrezIds);
    }

    private ClosenessSeedPayloadDTO getClosenessPayload(List<String> entrezIds) {
        ClosenessSeedPayloadDTO payload = new ClosenessSeedPayloadDTO();
        payload.setN(10);
        payload.setOnlyApprovedDrugs(false);
        payload.setOnlyDirectDrugs(false);
        payload.setSeeds(entrezIds);
        return payload;
    }

    private TrustRankSeedPayloadDTO getTrustrankPayload(List<String> entrezIds) {
        TrustRankSeedPayloadDTO payload = new TrustRankSeedPayloadDTO();
        payload.setN(10);
        payload.setDampingFactor(0.85);
        payload.setOnlyApprovedDrugs(false);
        payload.setOnlyDirectDrugs(false);
        payload.setSeeds(entrezIds);
        return payload;
    }
}
