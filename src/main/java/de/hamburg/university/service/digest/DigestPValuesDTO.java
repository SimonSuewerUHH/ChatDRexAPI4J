package de.hamburg.university.service.digest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigestPValuesDTO {
    Map<String, Map<String, Double>> values;
}
