package de.hamburg.university.helper.drugstone.dto;

import lombok.Data;

import java.util.List;

@Data
public class DrugstOneNetworkDTO {
    private List<DrugstOneNodeDTO> nodes;
    private List<DrugstOneEdgeDTO> edges;
    private String networkType;
}
