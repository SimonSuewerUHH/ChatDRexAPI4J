package de.hamburg.university.service.mygene;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyGeneGoEntryDTO {
    private Map<String, List<MyGeneGoTermDTO>> categories = new HashMap<>();

    @JsonAnySetter
    public void addCategory(String key, List<MyGeneGoTermDTO> value) {
        categories.put(key, value);
    }
}
