package de.hamburg.university.helper.drugstone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugstOneGroupsConfigColorDTO {
    private String border;
    private String background;
    private Highlight highlight;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DrugstOneGroupsConfigColorDTO that = (DrugstOneGroupsConfigColorDTO) o;
        return Objects.equals(getBorder(), that.getBorder()) && Objects.equals(getBackground(), that.getBackground()) && Objects.equals(getHighlight(), that.getHighlight());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBorder(), getBackground(), getHighlight());
    }

    @Override
    public String toString() {
        return "DrugstOneGroupsConfigColorDTO{" +
                "border='" + border + '\'' +
                ", background='" + background + '\'' +
                ", highlight=" + highlight +
                '}';
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Highlight {
        private String border;
        private String background;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Highlight highlight = (Highlight) o;
            return Objects.equals(getBorder(), highlight.getBorder()) && Objects.equals(getBackground(), highlight.getBackground());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getBorder(), getBackground());
        }

        @Override
        public String toString() {
            return "Highlight{" +
                    "border='" + border + '\'' +
                    ", background='" + background + '\'' +
                    '}';
        }
    }
}
