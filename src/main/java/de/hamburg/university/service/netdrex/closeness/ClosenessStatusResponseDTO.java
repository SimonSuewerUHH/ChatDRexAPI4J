package de.hamburg.university.service.netdrex.closeness;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.hamburg.university.service.netdrex.NetdrexStatusResponseDTO;
import de.hamburg.university.service.netdrex.trustrank.TrustRankStatusResultDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
//YES trustrank has same response structure as closeness
public class ClosenessStatusResponseDTO extends NetdrexStatusResponseDTO<TrustRankStatusResultDTO> {


}
