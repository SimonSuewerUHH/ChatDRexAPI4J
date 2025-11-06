package de.hamburg.university.tool.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.platform.commons.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiCypher {
    private String cypher;
    private List<Map<String, String>> results;
    private int attempts;
    private List<String> previousCyphers;
    private List<String> errors;

    public void moveCypherToHistory() {
        if (StringUtils.isNotBlank(this.cypher)) {
            if(this.previousCyphers == null) {
                this.previousCyphers = new ArrayList<>();
            }
            this.previousCyphers.add(this.cypher);
            this.cypher = "";
        }
    }

    public void addError(String error) {
        if(this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }
}
