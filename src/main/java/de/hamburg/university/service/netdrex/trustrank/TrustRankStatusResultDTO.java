package de.hamburg.university.service.netdrex.trustrank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrustRankStatusResultDTO {
    @JsonProperty("seed_proteins")
    private List<String> seedProteins;
    private List<TrustrankNodeDTO> drugs;
    private List<List<String>> edges;
}
