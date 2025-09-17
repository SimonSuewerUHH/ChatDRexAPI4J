package de.hamburg.university.agent.memory;

import de.hamburg.university.agent.planning.PlanState;
import de.hamburg.university.helper.drugstone.DrugstOneNetworkDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlanStateResult {
    private String userGoal;

    private String netdrexKgInfo = "";
    private String enhancedQueryBioInfo = "";

    private String resultSummary = "";

    public PlanStateResult(PlanState planState, String resultSummary) {
        this.userGoal = planState.getUserGoal();
        this.netdrexKgInfo = planState.getNetdrexKgInfo();
        this.enhancedQueryBioInfo = planState.getEnhancedQueryBioInfo();
        this.resultSummary = resultSummary;
    }
}
