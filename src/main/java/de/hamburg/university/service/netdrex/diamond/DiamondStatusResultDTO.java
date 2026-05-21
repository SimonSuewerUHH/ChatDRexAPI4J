package de.hamburg.university.service.netdrex.diamond;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DiamondStatusResultDTO {

    @JsonProperty("diamond_nodes")
    private List<DiamondNodeDTO> diamondNodes;
    @JsonProperty("seeds_in_network")
    private List<String> seedsInNetwork;
    private List<Object> edges;

}
