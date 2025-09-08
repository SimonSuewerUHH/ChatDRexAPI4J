package de.hamburg.university.agent.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.agent.bot.DigestBot;
import de.hamburg.university.agent.bot.FinalizeBot;
import de.hamburg.university.agent.bot.NetdrexBot;
import de.hamburg.university.agent.bot.ResearchBot;
import de.hamburg.university.agent.memory.InMemoryStateHolder;
import de.hamburg.university.agent.memory.PlanStateResult;
import de.hamburg.university.agent.planning.bots.DecisionPlannerBot;
import de.hamburg.university.agent.tool.netdrex.NetdrexTool;
import de.hamburg.university.agent.tool.netdrex.kg.NetdrexKGTool;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PlanningAgent {
    private static final int MAX_STEPS = 6;

    @Inject
    ChatWebsocketSender chatWebsocketSender;

    @Inject
    DecisionPlannerBot planner;

    @Inject
    ResearchBot research;

    @Inject
    FinalizeBot finalizeBot;

    @Inject
    NetdrexKGTool netdrexKGTool;

    @Inject
    NetdrexBot netdrexBot;

    @Inject
    DigestBot digestBot;

    @Inject
    NetdrexTool netdrexTool;

    @Inject
    InMemoryStateHolder stateHolder;

    private final ObjectMapper om = new ObjectMapper();

    public AgentResult planAnswer(ChatRequestDTO content, String context, MultiEmitter<? super ChatResponseDTO> emitter) {
        PlanState state = new PlanState();
        state.setPreviousContext(context);
        state.setUserGoal(content.getMessage());

        List<PlanStep> history = new ArrayList<>();
        emitter.emit(ChatResponseDTO.createReasoningResponse(content, "Start planing ..."));
        String connectionId = content.getConnectionId();
        for (int step = 1; step <= MAX_STEPS; step++) {
            int stepLeft = MAX_STEPS - step;
            PlanStep decision = planner.decide(state, history, stepLeft);
            history.add(decision);
            emitter.emit(ChatResponseDTO.createReasoningResponse(content, decision.getAction() + "->" + decision.getReason()));

            Log.debugf("Planning step %d: %s", step, safeToString(decision));

            if (decision.getAction() == null) break;

            switch (decision.getAction()) {
                case UPDATE_NETWORK -> {
                    Log.debugf("Action UPDATE_NETWORK: %s", decision.getReason());
                    // FUTURE NOT YET IMPLEMENTED
                    continue;
                }
                case FETCH_RESEARCH -> {
                    Log.debugf("Action FETCH_RESEARCH: %s", decision.getReason());
                    state.getResearch().add(research.answer(connectionId, state.getUserGoal()));
                }
                case FETCH_KG -> {
                    Log.debugf("Action FETCH_KG: %s", decision.getReason());
                    state.setNetdrexKgInfo(netdrexKGTool.answer(state.getUserGoal(), content, emitter));
                }
                case FETCH_BIO_INFO -> {
                    setEnhancedQueryBioInfo(state, decision, connectionId);
                }
                case CALL_NETDREX_TOOL -> {
                    Log.debugf("Action CALL_NETDREX_TOOL: %s", decision.getReason());
                    if (StringUtils.isEmpty(state.getEnhancedQueryBioInfo())) {
                        setEnhancedQueryBioInfoEnrezId(state, decision, connectionId);
                    }
                    state = netdrexTool.answer(state, content, emitter);
                }
                case CALL_DIGEST_TOOL -> {
                    Log.debugf("Action CALL_DIGEST_TOOL: %s", decision.getReason());
                    if (StringUtils.isEmpty(state.getEnhancedQueryBioInfo())) {
                        setEnhancedQueryBioInfoEnrezId(state, decision, connectionId);
                    }
                    state.setDigestResult(digestBot.answer(connectionId, state.getUserGoal(), state.getEnhancedQueryBioInfo()));
                }
                case FINALIZE -> {
                    // Stream all chunks from the finalize bot and emit each part
                    String result = finalizeBot.answer(connectionId, content.getMessage(), state)
                            .onItem().invoke(chunk -> emitter.emit(ChatResponseDTO.createAIResponse(content, chunk)))
                            .onFailure().invoke(t -> emitter.emit(ChatResponseDTO.createAIResponse(content, t.getMessage())))
                            .onCompletion().invoke(() -> emitter.emit(ChatResponseDTO.createAIResponse(content, "Finalized plan.")))
                            .collect()
                            .asList()
                            .map(list -> String.join("", list))
                            .await()
                            .indefinitely();

                    stateHolder.addState(connectionId, new PlanStateResult(state, result));
                    return new AgentResult(result);
                }
            }
        }
        return new AgentResult("Could not complete planning. Try asking: \"recommend a model for <task>\" or provide a workflow id for data-fit analysis.");
    }


    private void setEnhancedQueryBioInfo(PlanState state, PlanStep decision, String connectionId) {
        Log.debugf("Action FETCH_BIO_INFO: %s", decision.getReason());
        state.setEnhancedQueryBioInfo(netdrexBot.answer(connectionId, state.getUserGoal()));
    }

    private void setEnhancedQueryBioInfoEnrezId(PlanState state, PlanStep decision, String connectionId) {
        Log.debugf("Action FETCH_BIO_INFO: %s", decision.getReason());
        state.setEnhancedQueryBioInfo(netdrexBot.answerEntrezId(connectionId, state.getUserGoal()));
    }


    private String safeToString(PlanStep d) {
        try {
            return om.writeValueAsString(d);
        } catch (Exception e) {
            return String.valueOf(d);
        }
    }
}
