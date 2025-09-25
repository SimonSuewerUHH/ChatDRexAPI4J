package de.hamburg.university.service.digest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigestResultResponseDTO {
    private String task;
    private DigestResultsDTO result;
    private DigestToolResponseParametersDTO parameters;
}
