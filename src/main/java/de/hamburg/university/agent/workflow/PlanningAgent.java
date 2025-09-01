package de.hamburg.university.agent.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.agent.workflow.bots.DecisionPlannerBot;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class PlanningAgent {
    private static final int MAX_STEPS = 6;

    @Inject
    DecisionPlannerBot planner;


    private final ObjectMapper om = new ObjectMapper();

    public AgentResult planAnswer(Long workflowId, String userGoal) {
        PlanState state = new PlanState();
        state.setUserGoal(userGoal);
        state.setWorkflowId(workflowId);

        for (int step = 1; step <= MAX_STEPS; step++) {
            PlanStep decision = planner.decide(state);
            Log.debugf("Planning step %d: %s", step, safeToString(decision));

            if (decision == null || decision.getAction() == null) break;

            switch (decision.getAction()) {
                default -> {
                    if (StringUtils.isEmpty(decision.getMessageMarkdown())) {
                        decision.setMessageMarkdown("No summary produced.");
                    }
                    return new AgentResult(decision.getMessageMarkdown());
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
