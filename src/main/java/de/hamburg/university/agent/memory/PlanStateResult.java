package de.hamburg.university.agent.memory;

import de.hamburg.university.agent.planning.PlanState;
import lombok.Data;

@Data
public class PlanStateResult {
    private String userGoal;

    private String nedrexKgInfo = "";
    private String enhancedQueryBioInfo = "";

    private String resultSummary = "";

    public PlanStateResult(PlanState planState, String resultSummary) {
        this.userGoal = planState.getUserGoal();
        this.nedrexKgInfo = planState.getNeDRexKgInfo();
        this.enhancedQueryBioInfo = planState.getEnhancedQueryBioInfo();
        this.resultSummary = resultSummary;
    }
}
