package de.hamburg.university.service.mygene;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyGeneGoTermDTO {
    @JsonProperty("evidence")
    private String evidence;

    @JsonProperty("gocategory")
    private String goCategory;

    @JsonProperty("id")
    private String id;

    @JsonProperty("qualifier")
    private String qualifier;

    @JsonProperty("term")
    private String term;

    @JsonProperty("pubmed")
    private String pubmed;

}
