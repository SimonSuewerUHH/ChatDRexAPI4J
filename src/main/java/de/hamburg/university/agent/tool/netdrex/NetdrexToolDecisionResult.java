package de.hamburg.university.agent.tool.netdrex;

import lombok.Data;

import java.util.List;

@Data
public class NetdrexToolDecisionResult {
    private String toolName;
    private List<String> entrezIds;
}
