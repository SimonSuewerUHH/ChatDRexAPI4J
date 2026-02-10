package de.hamburg.university.agent.planning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanStep {
    private PlanAction action;
    private String reason;
    private String subTaskQuestion;
}
