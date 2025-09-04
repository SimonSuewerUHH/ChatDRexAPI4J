package de.hamburg.university.agent.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.agent.bot.FinalizeBot;
import de.hamburg.university.agent.bot.ResearchBot;
import de.hamburg.university.agent.planning.bots.DecisionPlannerBot;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class PlanningAgent {
    private static final int MAX_STEPS = 6;

    @Inject
    DecisionPlannerBot planner;

    @Inject
    ResearchBot research;

    @Inject
    FinalizeBot finalizeBot;

    private final ObjectMapper om = new ObjectMapper();

    public AgentResult planAnswer(ChatRequestDTO content, MultiEmitter<? super ChatResponseDTO> emitter) {
        PlanState state = new PlanState();
        state.setUserGoal(content.getMessage());

        emitter.emit(ChatResponseDTO.createReasoningResponse(content, "Start planing ..."));
        for (int step = 1; step <= MAX_STEPS; step++) {
            PlanStep decision = planner.decide(state);
            emitter.emit(ChatResponseDTO.createReasoningResponse(content, decision.getAction() + "->" + decision.getReason()));

            Log.debugf("Planning step %d: %s", step, safeToString(decision));

            if (decision == null || decision.getAction() == null) break;

            switch (decision.getAction()) {
                case FETCH_NETWORK -> {
                    if (StringUtils.isEmpty(decision.getMessageMarkdown())) {
                        decision.setMessageMarkdown("Fetched network.");
                    }
                    Log.debugf("Action FETCH_NETWORK: %s", decision.getMessageMarkdown());
                    // let the planner decide the next step after fetching
                    continue;
                }
                case UPDATE_NETWORK -> {
                    if (StringUtils.isEmpty(decision.getMessageMarkdown())) {
                        decision.setMessageMarkdown("Updated network.");
                    }
                    Log.debugf("Action UPDATE_NETWORK: %s", decision.getMessageMarkdown());
                    // proceed to the next planning step after update
                    continue;
                }
                case FETCH_RESEARCH -> {
                    if (StringUtils.isEmpty(decision.getMessageMarkdown())) {
                        decision.setMessageMarkdown("Fetched research results.");
                    }
                    Log.debugf("Action FETCH_RESEARCH: %s", decision.getMessageMarkdown());
                    state.getResearch().add(research.answer(state.getUserGoal()));
                    continue;
                }
                case FETCH_CHATDREX -> {
                    if (StringUtils.isEmpty(decision.getMessageMarkdown())) {
                        decision.setMessageMarkdown("Fetched ChatDrex context.");
                    }
                    Log.debugf("Action FETCH_CHATDREX: %s", decision.getMessageMarkdown());
                    // proceed to the next planning step after fetching ChatDrex data
                    continue;
                }
                case CALL_CHATDREX_TOOL -> {
                    if (StringUtils.isEmpty(decision.getMessageMarkdown())) {
                        decision.setMessageMarkdown("Called ChatDrex tool.");
                    }
                    Log.debugf("Action CALL_CHATDREX_TOOL: %s", decision.getMessageMarkdown());
                    // proceed to the next planning step after tool call
                    continue;
                }
                case FINALIZE -> {
                    if (StringUtils.isEmpty(decision.getMessageMarkdown())) {
                        decision.setMessageMarkdown("No summary produced.");
                    }
                    // Stream all chunks from the finalize bot and emit each part
                    String result = finalizeBot.answer(content.getMessage(), state)
                            .onItem().invoke(chunk -> emitter.emit(ChatResponseDTO.createAIResponse(content, chunk)))
                            .onFailure().invoke(t -> emitter.emit(ChatResponseDTO.createAIResponse(content, t.getMessage())))
                            .onCompletion().invoke(() -> emitter.emit(ChatResponseDTO.createAIResponse(content, "Finalized plan.")))
                            .collect()
                            .asList()
                            .map(list -> String.join("", list))
                            .await()
                            .indefinitely();


                    return new AgentResult(result);
                }
            }
        }
        return new AgentResult("Could not complete planning. Try asking: \"recommend a model for <task>\" or provide a workflow id for data-fit analysis.");
    }


    private String safeToString(PlanStep d) {
        try {
            return om.writeValueAsString(d);
        } catch (Exception e) {
            return String.valueOf(d);
        }
    }
}
