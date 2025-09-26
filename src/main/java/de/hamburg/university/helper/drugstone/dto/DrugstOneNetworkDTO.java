package de.hamburg.university.helper.drugstone.dto;

import lombok.Data;

import java.util.List;

@Data
public class DrugstOneNetworkDTO {
    private List<DrugstOneNodeDTO> nodes;
    private List<DrugstOneEdgeDTO> edges;
    private String networkType;


    public void patch(DrugstOneNetworkDTO other) {
        if (other == null) return;
        if (other.getNodes() != null) {
            if (this.nodes == null) {
                this.setNodes(other.getNodes());
            } else {
                this.nodes.addAll(other.getNodes());
            }
        }
        if (other.getEdges() != null) {
            if (this.edges == null) {
                this.setEdges(other.getEdges());
            } else {
                this.edges.addAll(other.getEdges());
            }
        }
        if (other.getNetworkType() != null) this.setNetworkType(other.getNetworkType());
    }
}
