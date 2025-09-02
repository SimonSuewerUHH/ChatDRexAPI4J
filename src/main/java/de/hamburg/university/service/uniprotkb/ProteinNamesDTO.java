package de.hamburg.university.service.uniprotkb;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProteinNamesDTO {
    private String recommendedName;
    private List<String> shortNames;
    private List<String> alternativeFull;
    private List<String> alternativeShort;
}
