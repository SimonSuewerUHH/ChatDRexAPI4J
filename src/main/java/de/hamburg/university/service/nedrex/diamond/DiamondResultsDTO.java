package de.hamburg.university.service.nedrex.diamond;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiamondResultsDTO {
    private List<String> seeds;
    @JsonProperty("diamond_nodes")
    private List<String> diamondNodes;
    private List<Object> edges;

}
