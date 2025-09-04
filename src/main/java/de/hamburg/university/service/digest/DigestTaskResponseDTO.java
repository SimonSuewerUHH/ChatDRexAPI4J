package de.hamburg.university.service.digest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigestTaskResponseDTO {
    private String task;

}
