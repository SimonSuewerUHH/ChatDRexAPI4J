package de.hamburg.university.service.nedrex.diamond;

import de.hamburg.university.service.nedrex.BaseNedrexTool;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class DiamondToolClientService extends BaseNedrexTool<DiamondApiClient, SeedPayloadDTO, DiamondStatusResponseDTO, DiamondStatusResultDTO, DiamondResultsDTO> {

    @Override
    protected DiamondResultsDTO mapResult(DiamondStatusResultDTO result) {
        List<String> diamondNodes = result.getDiamondNodes().stream().map(DiamondNodeDTO::getDiamondNode).collect(Collectors.toList());
        return new DiamondResultsDTO(result.getSeedsInNetwork(), diamondNodes, result.getEdges());
    }

}
