package de.hamburg.university.agent.workflow;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PlanStep {
    private PlanAction action;

    private String reason;

    private Map<String, Object> args = new HashMap<>();

    private String messageMarkdown;
}
