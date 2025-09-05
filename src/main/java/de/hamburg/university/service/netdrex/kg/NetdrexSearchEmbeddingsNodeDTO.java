package de.hamburg.university.service.netdrex.kg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class NetdrexSearchEmbeddingsNodeDTO {
    @JsonProperty("n.primaryDomainId")
    private String primaryDomainId;
    @JsonProperty("n.dataSources")
    private List<String> dataSources;
    @JsonProperty("n.displayName")
    private String displayName;

    @JsonProperty("n.description")
    private String description;

    @JsonProperty("n.synonyms")
    private List<String> synonyms;

}
