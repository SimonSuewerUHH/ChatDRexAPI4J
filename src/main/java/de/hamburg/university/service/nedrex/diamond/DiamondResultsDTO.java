package de.hamburg.university.service.nedrex.diamond;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiamondResultsDTO {
    private List<String> seeds;
    @JsonProperty("diamond_nodes")
    private List<String> diamondNodes;
    private List<Object> edges;

    @JsonProperty("edges")
    @SuppressWarnings("unchecked")
    public void setEdges(Object edges) {
        if (edges instanceof List) {
            this.edges = (List<Object>) edges;
        } else {
            this.edges = List.of(edges);
        }
    }
}
