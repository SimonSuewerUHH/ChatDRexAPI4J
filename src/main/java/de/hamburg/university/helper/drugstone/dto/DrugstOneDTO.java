package de.hamburg.university.helper.drugstone.dto;

import lombok.Data;

@Data
public class DrugstOneDTO {
    private DrugstOneNetworkDTO network = new DrugstOneNetworkDTO();
    private DrugstOneGroupsConfigDTO groups = new DrugstOneGroupsConfigDTO();
    private DrugstOneConfigDTO config;
}
