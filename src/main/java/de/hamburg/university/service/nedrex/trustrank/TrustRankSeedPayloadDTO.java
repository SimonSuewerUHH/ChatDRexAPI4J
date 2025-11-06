package de.hamburg.university.service.nedrex.trustrank;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TrustRankSeedPayloadDTO {
    private List<String> seeds;

    @JsonProperty(value = "damping_factor")
    private Double dampingFactor;

    @JsonProperty(value = "only_direct_drugs")
    private boolean onlyDirectDrugs;

    @JsonProperty(value = "only_approved_drugs")
    private boolean onlyApprovedDrugs;

    @JsonProperty(value = "N")
    private Integer n;
}

