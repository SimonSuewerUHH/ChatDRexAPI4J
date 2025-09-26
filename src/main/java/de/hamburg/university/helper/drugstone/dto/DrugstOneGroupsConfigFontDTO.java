package de.hamburg.university.helper.drugstone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugstOneGroupsConfigFontDTO {
    private String color;
    private Integer size;
    private String face;
    private Integer strokeWidth;
    private String strokeColor;
    private String align;
    private Boolean bold;
    private Boolean ital;
    private Boolean boldital;
    private Boolean mono;


    @Override
    public String toString() {
        return "DrugstOneGroupsConfigFontDTO{" +
                "color='" + color + '\'' +
                ", size=" + size +
                ", face='" + face + '\'' +
                ", strokeWidth=" + strokeWidth +
                ", strokeColor='" + strokeColor + '\'' +
                ", align='" + align + '\'' +
                ", bold=" + bold +
                ", ital=" + ital +
                ", boldital=" + boldital +
                ", mono=" + mono +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DrugstOneGroupsConfigFontDTO font = (DrugstOneGroupsConfigFontDTO) o;
        return Objects.equals(getColor(), font.getColor()) && Objects.equals(getSize(), font.getSize()) && Objects.equals(getFace(), font.getFace()) && Objects.equals(getStrokeWidth(), font.getStrokeWidth()) && Objects.equals(getStrokeColor(), font.getStrokeColor()) && Objects.equals(getAlign(), font.getAlign()) && Objects.equals(getBold(), font.getBold()) && Objects.equals(getItal(), font.getItal()) && Objects.equals(getBoldital(), font.getBoldital()) && Objects.equals(getMono(), font.getMono());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getColor(), getSize(), getFace(), getStrokeWidth(), getStrokeColor(), getAlign(), getBold(), getItal(), getBoldital(), getMono());
    }
}
