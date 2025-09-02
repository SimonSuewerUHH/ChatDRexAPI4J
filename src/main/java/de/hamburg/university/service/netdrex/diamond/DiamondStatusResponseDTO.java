package de.hamburg.university.service.netdrex.diamond;

import de.hamburg.university.service.netdrex.NetdrexStatusResponseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DiamondStatusResponseDTO extends NetdrexStatusResponseDTO<DiamondStatusResultDTO> {


}
