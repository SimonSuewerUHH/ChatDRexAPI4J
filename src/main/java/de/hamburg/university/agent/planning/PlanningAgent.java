package de.hamburg.university.agent.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.agent.bot.DIGESTBot;
import de.hamburg.university.agent.bot.FinalizeBot;
import de.hamburg.university.agent.bot.NeDRexBot;
import de.hamburg.university.agent.bot.ResearchBot;
import de.hamburg.university.agent.memory.InMemoryStateHolder;
import de.hamburg.university.agent.memory.PlanStateResult;
import de.hamburg.university.agent.planning.bots.DecisionPlannerBot;
import de.hamburg.university.agent.tool.nedrex.NeDRexTool;
import de.hamburg.university.agent.tool.nedrex.kg.NeDRexKGTool;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
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
    ChatMemoryStore chatMemoryStore;

    @Inject
    InMemoryStateHolder stateHolder;

    @Inject
    DecisionPlannerBot planner;

    @Inject
    ResearchBot research;

    @Inject
    FinalizeBot finalizeBot;

    @Inject
    NeDRexKGTool nedrexKGTool;

    @Inject
    NeDRexBot neDRexBot;

    @Inject
    DIGESTBot digestBot;

    @Inject
    NeDRexTool neDRexTool;

    private final ObjectMapper om = new ObjectMapper();

    public AgentResult planAnswer(ChatRequestDTO content, String context, MultiEmitter<? super ChatResponseDTO> emitter) {
        PlanState state = new PlanState();
        state.setPreviousContext(context);
        state.setUserGoal(content.getMessage());

        List<PlanStep> history = new ArrayList<>();
        emitter.emit(ChatResponseDTO.createReasoningResponse(content, reasonToHtml()));
        String connectionId = content.getConnectionId();
        resetMemory(content, state, 0);

        for (int step = 1; step <= MAX_STEPS; step++) {
            int stepLeft = MAX_STEPS - step;
            PlanStep decision = planner.decide(state, history, stepLeft);
            history.add(decision);
            emitter.emit(ChatResponseDTO.createReasoningResponse(content, reasonToHtml(decision)));

            String currentGoal = decision.getSubTaskQuestion();
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
                    state.getResearch().add(research.answer(connectionId, currentGoal, state.getPreviousContext()));
                }
                case FETCH_KG -> {
                    Log.debugf("Action FETCH_KG: %s", decision.getReason());
                    state.setNeDRexKgInfo(nedrexKGTool.answer(currentGoal, state.getPreviousContext(), content, emitter));
                }
                case FETCH_BIO_INFO -> {
                    setEnhancedQueryBioInfo(state, decision, connectionId);
                }
                case CALL_NEDREX_TOOL -> {
                    Log.debugf("Action CALL_NEDREX_TOOL: %s", decision.getReason());
                    if (StringUtils.isEmpty(state.getEnhancedQueryBioInfo())) {
                        setEnhancedQueryBioInfoEnrezId(state, decision, connectionId);
                    }
                    state = neDRexTool.answer(state, content, emitter);
                }
                case CALL_DIGEST_TOOL -> {
                    Log.debugf("Action CALL_DIGEST_TOOL: %s", decision.getReason());
                    if (StringUtils.isEmpty(state.getEnhancedQueryBioInfo())) {
                        setEnhancedQueryBioInfoEnrezId(state, decision, connectionId);
                    }
                    state.setDigestResult(digestBot.answer(connectionId, currentGoal, state.getEnhancedQueryBioInfo()));
                }
                case FINALIZE -> {
                    // Stream all chunks from the finalize bot and emit each part
                    String result = finalizeBot.answer(connectionId, content.getMessage(), state)
                            .onItem().invoke(chunk -> emitter.emit(ChatResponseDTO.createAIResponse(content, chunk)))
                            .onFailure().invoke(t -> emitter.emit(ChatResponseDTO.createAIResponse(content, t.getMessage())))
                            .onCompletion().invoke(() -> emitter.emit(ChatResponseDTO.createAIResponse(content, "")))
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
        state.setEnhancedQueryBioInfo(neDRexBot.answer(connectionId, decision.getSubTaskQuestion(), state.getPreviousContext()));
    }

    private void setEnhancedQueryBioInfoEnrezId(PlanState state, PlanStep decision, String connectionId) {
        Log.debugf("Action FETCH_BIO_INFO: %s", decision.getReason());
        state.setEnhancedQueryBioInfo(neDRexBot.answerEntrezId(connectionId, decision.getSubTaskQuestion(), state.getPreviousContext()));
    }


    private String safeToString(PlanStep d) {
        try {
            return om.writeValueAsString(d);
        } catch (Exception e) {
            return String.valueOf(d);
        }
    }

    private void resetMemory(ChatRequestDTO content, PlanState state, int step) {
        Log.infof("Clean memory for step %d", step);
        chatMemoryStore.deleteMessages(content.getConnectionId());
        if (StringUtils.isNotEmpty(state.getPreviousContext())) {
            Log.infof("Resetting memory for %s", state.getPreviousContext());
            UserMessage context = new UserMessage(state.getPreviousContext());
            ArrayList<ChatMessage> messages = new ArrayList<>();
            messages.add(context);
            chatMemoryStore.updateMessages(content.getConnectionId(), messages);
        }
    }

    public String reasonToHtml() {
        return "<div class=\"step\">" +
                "<span class=\"action\"><span class=\"head\">Action:</span>Start planning ...</span>" +
                "</div>";
    }

    public String reasonToHtml(PlanStep decision) {
        if (decision == null) return "";

        return "<div class=\"step\">" +
                "<span class=\"action\"><span class=\"head\">Action:</span>" + decision.getAction() + "</span>" +
                "<span class=\"reason\"><span class=\"head\">Reason:</span>" + decision.getReason() + "</span>" +
                "<span class=\"subtask\"><span class=\"head\">Sub-task:</span>" + decision.getSubTaskQuestion() + "</span>" +
                "</div>";
    }
}
