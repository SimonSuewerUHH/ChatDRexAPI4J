package de.hamburg.university.service.netdrex.trustrank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrustRankResultDTO {
    private List<String> seedGenes;
    private List<String> seedProteins;
    private List<String> drugs;
    private List<String> drugbankIds;
    private TrustRankResultDTO toolOutput;
    private Instant lastUpdate;
}
