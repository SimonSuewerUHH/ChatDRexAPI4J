package de.hamburg.university.service.digest;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DigestToolResultDTO {
    private List<Row> rows;
    private String task;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Row {
        private String database;
        private List<String> gene;
        private LinkedHashMap<String, Integer> dbTerms;
        private Double empiricalPValue;
        private List<String> description;
    }
}