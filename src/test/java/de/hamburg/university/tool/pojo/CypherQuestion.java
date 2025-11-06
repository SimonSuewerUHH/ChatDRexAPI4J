package de.hamburg.university.tool.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CypherQuestion {
    @JsonProperty("nl_question")
    private String nlQuestion;

    @JsonProperty("cypher_translation")
    private String cypherTranslation;
}
