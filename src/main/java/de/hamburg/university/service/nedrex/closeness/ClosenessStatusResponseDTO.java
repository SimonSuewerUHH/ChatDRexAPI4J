package de.hamburg.university.service.nedrex.closeness;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.hamburg.university.service.nedrex.NeDRexStatusResponseDTO;
import de.hamburg.university.service.nedrex.trustrank.TrustRankStatusResultDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
//YES trustrank has same response structure as closeness
public class ClosenessStatusResponseDTO extends NeDRexStatusResponseDTO<TrustRankStatusResultDTO> {


}
