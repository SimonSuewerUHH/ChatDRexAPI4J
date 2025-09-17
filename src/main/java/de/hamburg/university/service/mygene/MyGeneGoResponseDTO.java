package de.hamburg.university.service.mygene;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyGeneGoResponseDTO {
    @JsonProperty("_id")
    private String id;

    @JsonProperty("_score")
    private double score;

    @JsonProperty("go")
    private MyGeneGoEntryDTO go;
}
