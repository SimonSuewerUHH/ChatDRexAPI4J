package de.hamburg.university.service.netdrex.diamond;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DiamondNodeDTO {
    @JsonProperty("DIAMOnD_node")
    private String diamondNode;

    @JsonProperty("p_hyper")
    private String pHyper;
    private String rank;

}
