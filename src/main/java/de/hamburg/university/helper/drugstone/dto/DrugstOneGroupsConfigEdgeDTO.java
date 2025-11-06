package de.hamburg.university.helper.drugstone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugstOneGroupsConfigEdgeDTO {
    private String groupName;
    private String color;
    private Boolean dashes;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DrugstOneGroupsConfigEdgeDTO that = (DrugstOneGroupsConfigEdgeDTO) o;
        return Objects.equals(getGroupName(), that.getGroupName()) && Objects.equals(getColor(), that.getColor()) && Objects.equals(getDashes(), that.getDashes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroupName(), getColor(), getDashes());
    }

    @Override
    public String toString() {
        return "DrugstOneGroupsConfigEdgeDTO{" +
                "groupName='" + groupName + '\'' +
                ", color='" + color + '\'' +
                ", dashes=" + dashes +
                '}';
    }
}
