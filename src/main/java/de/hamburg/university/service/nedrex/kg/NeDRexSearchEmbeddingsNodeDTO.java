package de.hamburg.university.service.nedrex.kg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class NeDRexSearchEmbeddingsNodeDTO {
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
