package de.hamburg.university.service.research.pubmed;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PubMedSearchResultEntryDTO {

    private String count;

    @JsonProperty("retmax")
    private String retMax;

    @JsonProperty("retstart")
    private String retStart;

    @JsonProperty("querykey")
    private String queryKey;

    @JsonProperty("webenv")
    private String webEnv;

    @JsonProperty("idlist")
    private List<String> idList;
}
