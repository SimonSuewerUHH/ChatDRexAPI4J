package de.hamburg.university.tool.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AiCypher {
    private String cypher;
    private List<Map<String, String>> results;

    public AiCypher(String cypher) {
        this.cypher = cypher;
        this.results = List.of();
    }
}
