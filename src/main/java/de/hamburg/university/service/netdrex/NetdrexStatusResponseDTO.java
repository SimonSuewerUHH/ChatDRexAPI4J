package de.hamburg.university.service.netdrex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetdrexStatusResponseDTO<R> {
    private NetdrexStatus status;
    private R results;
}
