package de.hamburg.university.service.uniprotkb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganismDTO {
    private Integer taxonId;
    private String scientificName;
    private String commonName;
    private List<String> lineage;
}

