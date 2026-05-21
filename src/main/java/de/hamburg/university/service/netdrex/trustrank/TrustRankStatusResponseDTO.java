package de.hamburg.university.service.netdrex.trustrank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.hamburg.university.service.netdrex.NetdrexStatusResponseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrustRankStatusResponseDTO extends NetdrexStatusResponseDTO<TrustRankStatusResultDTO> {


}
