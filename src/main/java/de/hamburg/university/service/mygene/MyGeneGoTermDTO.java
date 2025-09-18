package de.hamburg.university.service.mygene;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

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
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> pubmed;

}
