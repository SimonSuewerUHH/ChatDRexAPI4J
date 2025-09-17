package de.hamburg.university.service.uniprotkb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneSimpleDTO {
    private GeneTextDTO primary;
    private GeneTextDTO geneName;
    private List<GeneTextDTO> synonyms;
    private List<GeneTextDTO> orderedLocusNames;
    private List<GeneTextDTO> orfNames;

    public boolean hasPrimaryName() {
        return primary != null && StringUtils.isNotEmpty(primary.getValue());
    }

    public boolean hasGeneName() {
        return geneName != null && StringUtils.isNotEmpty(geneName.getValue());
    }


}
