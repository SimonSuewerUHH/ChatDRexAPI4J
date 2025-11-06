package de.hamburg.university.service.nedrex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NeDRexStatusResponseDTO<R> {
    private NeDRexStatus status;
    private R results;
}
