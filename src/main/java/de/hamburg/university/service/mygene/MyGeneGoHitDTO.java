package de.hamburg.university.service.mygene;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyGeneGoHitDTO {

    @JsonProperty("took")
    private int took;

    @JsonProperty("total")
    private int total;

    @JsonProperty("max_score")
    private double maxScore;

    @JsonProperty("hits")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<MyGeneGoResponseDTO> hits;
}
