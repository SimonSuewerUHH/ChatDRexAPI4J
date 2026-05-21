package de.hamburg.university.service.netdrex.trustrank;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TrustrankNodeDTO {
    @JsonProperty("drug_name")
    private String drugName;

    @JsonProperty("score")
    private String score;
}
