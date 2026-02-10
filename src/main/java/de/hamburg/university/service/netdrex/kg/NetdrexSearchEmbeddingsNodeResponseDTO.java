package de.hamburg.university.service.netdrex.kg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetdrexSearchEmbeddingsNodeResponseDTO extends NetdrexSearchEmbeddingsNodeDTO {
    private Double score;
    @JsonProperty("n.type")
    private String type;
    @JsonProperty("n.dataSources")
    private List<String> domainIds;
}
