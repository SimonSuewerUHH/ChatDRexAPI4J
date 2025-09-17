package de.hamburg.university.service.research.pubmed;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PubMedSearchResultDTO {

    private PubMedSearchHeaderDTO header;

    @JsonProperty("esearchresult")
    private PubMedSearchResultEntryDTO result;

    @JsonProperty("querytranslation")
    private String queryTranslation;

    @JsonProperty("translationset")
    private List<PubMedSearchTranslationSetDTO> translationSet;
}
