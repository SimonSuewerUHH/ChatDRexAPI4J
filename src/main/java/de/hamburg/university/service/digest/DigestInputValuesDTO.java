package de.hamburg.university.service.digest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigestInputValuesDTO {
    Map<String, Map<String, Double>> values;
    @JsonProperty("mapped_ids")
    Map<String, Map<String, List<String>>> mappedIds;

}
