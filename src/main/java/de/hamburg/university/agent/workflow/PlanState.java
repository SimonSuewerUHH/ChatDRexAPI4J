package de.hamburg.university.agent.workflow;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlanState {
    private String userGoal;

    private Long workflowId;

    private List<String> research = new ArrayList<>();
    private List<String> dataProfiles = new ArrayList<>();
    private List<String> notes = new ArrayList<>();
}
