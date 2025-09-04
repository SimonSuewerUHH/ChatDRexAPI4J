package de.hamburg.university.service.digest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigestStatusResponseDTO {
    private String task;
    private Boolean failed;
    private Boolean done;
    private String status;
    private Map<String, Object> stats;
    private String mode;
    private String type;
    private Map<String, Object> input;
    private String progress;

}
