package de.hamburg.university.service.digest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigestResultsDTO {
    private DigestInputValuesDTO inputValues;
    private DigestPValuesDTO pValues;
}
