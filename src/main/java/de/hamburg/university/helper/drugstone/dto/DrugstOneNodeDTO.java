package de.hamburg.university.helper.drugstone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DrugstOneNodeDTO {
    private String id;
    private String label;
    @JsonProperty("group")
    private String type;
}
