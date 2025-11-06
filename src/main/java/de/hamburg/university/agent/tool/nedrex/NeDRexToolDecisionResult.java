package de.hamburg.university.agent.tool.nedrex;

import lombok.Data;

import java.util.List;

@Data
public class NeDRexToolDecisionResult {
    private String toolName;
    private List<String> entrezIds;
}
