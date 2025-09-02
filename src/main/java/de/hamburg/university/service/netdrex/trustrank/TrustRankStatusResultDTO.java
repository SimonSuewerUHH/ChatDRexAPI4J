package de.hamburg.university.service.netdrex.trustrank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrustRankStatusResultDTO {
    private List<TrustrankNodeDTO> drugs;
    private List<Object> edges;
}
