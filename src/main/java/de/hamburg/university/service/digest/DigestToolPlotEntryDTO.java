package de.hamburg.university.service.digest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigestToolPlotEntryDTO {
    private String database;
    private String term;
    private Integer score;
    private Double empiricalPValue;
    private String description;
    private String gene;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DigestToolPlotEntryDTO that = (DigestToolPlotEntryDTO) o;
        return Objects.equals(database, that.database) && Objects.equals(term, that.term) && Objects.equals(score, that.score) && Objects.equals(empiricalPValue, that.empiricalPValue) && Objects.equals(description, that.description) && Objects.equals(gene, that.gene);
    }

    @Override
    public int hashCode() {
        return Objects.hash(database, term, score, empiricalPValue, description, gene);
    }
}
