package de.hamburg.university.service.digest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigestResultsDTO {
    @JsonProperty("input_values")
    private DigestInputValuesDTO inputValues;
    @JsonProperty("p_values")
    private DigestPValuesDTO pValues;
}
