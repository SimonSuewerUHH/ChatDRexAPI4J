package de.hamburg.university.agent.planning;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlanState {
    private String userGoal;
    private String previousContext;

    private Long workflowId;

    private List<String> research = new ArrayList<>();
    private String netdrexKgInfo = "";
    private String digestResult = "";
    private String enhancedQueryBioInfo = "";

    private String networkSummary = "";
    List<String> agentAnswers = new ArrayList<>();

    public void addAgentAnswer(String answer) {
        if (agentAnswers == null) {
            agentAnswers = new ArrayList<>();
        }
        this.agentAnswers.add(answer);
    }
}
