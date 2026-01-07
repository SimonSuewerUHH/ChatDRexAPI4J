package de.hamburg.university.agent.tool.nedrex;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class NeDRexToolDecisionResult {
    private String toolName;
    private List<String> entrezIds;

    public List<String> getEntrezIds() {
        return Optional.ofNullable(entrezIds).orElse(new ArrayList<>());
    }
}
