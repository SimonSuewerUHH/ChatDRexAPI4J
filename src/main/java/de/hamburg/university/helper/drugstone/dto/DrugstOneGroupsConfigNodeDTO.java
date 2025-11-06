package de.hamburg.university.helper.drugstone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugstOneGroupsConfigNodeDTO {
    private String groupName;
    private Integer borderWidth;
    private DrugstOneGroupsConfigColorDTO color;
    private String shape;
    private String type;
    private Boolean detailShowLabel;
    private DrugstOneGroupsConfigFontDTO font;
    private Integer borderWidthSelected;

    @Override
    public String toString() {
        return "DrugstOneGroupsConfigNodeDTO{" +
                "groupName='" + groupName + '\'' +
                ", borderWidth=" + borderWidth +
                ", color=" + color +
                ", shape='" + shape + '\'' +
                ", type='" + type + '\'' +
                ", detailShowLabel=" + detailShowLabel +
                ", font=" + font +
                ", borderWidthSelected=" + borderWidthSelected +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DrugstOneGroupsConfigNodeDTO that = (DrugstOneGroupsConfigNodeDTO) o;
        return Objects.equals(getGroupName(), that.getGroupName()) && Objects.equals(getBorderWidth(), that.getBorderWidth()) && Objects.equals(getColor(), that.getColor()) && Objects.equals(getShape(), that.getShape()) && Objects.equals(getType(), that.getType()) && Objects.equals(getDetailShowLabel(), that.getDetailShowLabel()) && Objects.equals(getFont(), that.getFont()) && Objects.equals(getBorderWidthSelected(), that.getBorderWidthSelected());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroupName(), getBorderWidth(), getColor(), getShape(), getType(), getDetailShowLabel(), getFont(), getBorderWidthSelected());
    }
}
