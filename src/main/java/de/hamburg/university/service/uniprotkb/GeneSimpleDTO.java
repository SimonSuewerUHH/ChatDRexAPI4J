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
    private List<String> synonyms;
    private List<String> orderedLocusNames;
    private List<String> orfNames;

    public boolean hasPrimaryName() {
        return primary != null && StringUtils.isNotEmpty(primary.getValue());
    }

    public boolean hasGeneName() {
        return geneName != null && StringUtils.isNotEmpty(geneName.getValue());
    }


}
