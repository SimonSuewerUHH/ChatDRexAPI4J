package de.hamburg.university.service.uniprotkb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UniProtEntryDTO {
    private String primaryAccession;
    private String uniProtkbId;
    private boolean active;

    private ProteinNamesDTO protein;
    private List<GeneSimpleDTO> genes;
}
