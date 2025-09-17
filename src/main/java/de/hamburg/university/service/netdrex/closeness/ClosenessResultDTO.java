package de.hamburg.university.service.netdrex.closeness;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.hamburg.university.service.netdrex.trustrank.TrustRankResultDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClosenessResultDTO extends TrustRankResultDTO {

}
