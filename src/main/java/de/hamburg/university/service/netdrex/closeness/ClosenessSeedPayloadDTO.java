package de.hamburg.university.service.netdrex.closeness;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ClosenessSeedPayloadDTO {
    private List<String> seeds;

    @JsonProperty(value = "only_direct_drugs")
    private boolean onlyDirectDrugs;

    @JsonProperty(value = "only_approved_drugs")
    private boolean onlyApprovedDrugs;

    @JsonProperty(value = "N")
    private Integer n;
}

