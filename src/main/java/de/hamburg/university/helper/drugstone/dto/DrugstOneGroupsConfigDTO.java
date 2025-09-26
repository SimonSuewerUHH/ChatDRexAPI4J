package de.hamburg.university.helper.drugstone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugstOneGroupsConfigDTO {

    private Map<String, DrugstOneGroupsConfigNodeDTO> nodeGroups;
    private Map<String, DrugstOneGroupsConfigEdgeDTO> edgeGroups;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DrugstOneGroupsConfigDTO that = (DrugstOneGroupsConfigDTO) o;
        return Objects.equals(getNodeGroups(), that.getNodeGroups()) && Objects.equals(getEdgeGroups(), that.getEdgeGroups());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNodeGroups(), getEdgeGroups());
    }

    @Override
    public String toString() {
        return "DrugstOneGroupsConfigDTO{" +
                "nodeGroups=" + nodeGroups +
                ", edgeGroups=" + edgeGroups +
                '}';
    }
}
