package de.hamburg.university.service.netdrex.trustrank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrustRankResultDTO {
    private List<String> seedProteins;
    private List<String> drugNames;
    private List<TrustRankToolEdge> edges;

}
