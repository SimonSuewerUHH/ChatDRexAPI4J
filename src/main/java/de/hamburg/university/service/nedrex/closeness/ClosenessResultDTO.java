package de.hamburg.university.service.nedrex.closeness;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.hamburg.university.service.nedrex.trustrank.TrustRankResultDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClosenessResultDTO extends TrustRankResultDTO {

}
