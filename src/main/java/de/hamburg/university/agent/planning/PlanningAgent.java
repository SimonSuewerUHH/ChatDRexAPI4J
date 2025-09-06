package de.hamburg.university.agent.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.agent.bot.DigestBot;
import de.hamburg.university.agent.bot.FinalizeBot;
import de.hamburg.university.agent.bot.NetdrexBot;
import de.hamburg.university.agent.bot.ResearchBot;
import de.hamburg.university.agent.planning.bots.DecisionPlannerBot;
import de.hamburg.university.agent.tool.netdrex.NetdrexTool;
import de.hamburg.university.agent.tool.netdrex.kg.NetdrexKGTool;
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

    @Inject
    NetdrexKGTool netdrexKGTool;

    @Inject
    NetdrexBot netdrexBot;

    @Inject
    DigestBot digestBot;

    @Inject
    NetdrexTool netdrexTool;

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
                case UPDATE_NETWORK -> {
                    if (StringUtils.isEmpty(decision.getMessageMarkdown())) {
                        decision.setMessageMarkdown("Updated network.");
                    }
                    Log.debugf("Action UPDATE_NETWORK: %s", decision.getMessageMarkdown());
                    // FUTURE NOT YET IMPLEMENTED
                    continue;
                }
                case FETCH_RESEARCH -> {
                    if (StringUtils.isEmpty(decision.getMessageMarkdown())) {
                        decision.setMessageMarkdown("Fetched research results.");
                    }
                    Log.debugf("Action FETCH_RESEARCH: %s", decision.getMessageMarkdown());
                    state.getResearch().add(research.answer(state.getUserGoal()));
                }
                case FETCH_KG -> {
                    if (StringUtils.isEmpty(decision.getMessageMarkdown())) {
                        decision.setMessageMarkdown("Fetched Netdrex knowladge Graph context.");
                    }
                    Log.debugf("Action FETCH_KG: %s", decision.getMessageMarkdown());
                    state.setNetdrexKgInfo(netdrexKGTool.answer(state.getUserGoal()));
                }
                case FETCH_BIO_INFO -> {
                    setEnhancedQueryBioInfo(state, decision);
                }
                case CALL_NETDREX_TOOL -> {
                    if (StringUtils.isEmpty(decision.getMessageMarkdown())) {
                        decision.setMessageMarkdown("Called Netdrex tool.");
                    }
                    Log.debugf("Action CALL_NETDREX_TOOL: %s", decision.getMessageMarkdown());
                    if (StringUtils.isEmpty(state.getEnhancedQueryBioInfo())) {
                        setEnhancedQueryBioInfo(state, decision);
                    }
                    state = netdrexTool.answer(state);
                }
                case CALL_DIGEST_TOOL -> {
                    if (StringUtils.isEmpty(decision.getMessageMarkdown())) {
                        decision.setMessageMarkdown("Called Digest tool.");
                    }
                    Log.debugf("Action CALL_DIGEST_TOOL: %s", decision.getMessageMarkdown());
                    if (StringUtils.isEmpty(state.getEnhancedQueryBioInfo())) {
                        setEnhancedQueryBioInfo(state, decision);
                    }
                    state.setDigestResult(digestBot.answer(state.getUserGoal(), state.getEnhancedQueryBioInfo()));
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


    private void setEnhancedQueryBioInfo(PlanState state, PlanStep decision) {
        if (StringUtils.isEmpty(decision.getMessageMarkdown())) {
            decision.setMessageMarkdown("Fetched external bio info context.");
        }
        Log.debugf("Action FETCH_BIO_INFO: %s", decision.getMessageMarkdown());
        state.setEnhancedQueryBioInfo(netdrexBot.answer(state.getUserGoal()));
    }
    private String safeToString(PlanStep d) {
        try {
            return om.writeValueAsString(d);
        } catch (Exception e) {
            return String.valueOf(d);
        }
    }
}
