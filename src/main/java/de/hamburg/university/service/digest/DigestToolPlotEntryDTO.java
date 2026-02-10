package de.hamburg.university.service.digest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
