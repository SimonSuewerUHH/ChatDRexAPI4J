package de.hamburg.university.service.netdrex.closeness;

import de.hamburg.university.service.netdrex.BaseNedrexTool;
import de.hamburg.university.service.netdrex.trustrank.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ClosenessToolClientService extends BaseNedrexTool<ClosenessApiClient, ClosenessSeedPayloadDTO, ClosenessStatusResponseDTO, TrustRankStatusResultDTO, ClosenessResultDTO> {

    @Inject
    TrustRankToolClientService trustRankToolClientService;

    @Override
    protected ClosenessResultDTO mapResult(TrustRankStatusResultDTO result) {
        ClosenessResultDTO mappedResult = new ClosenessResultDTO();

        List<String> drugName = trustRankToolClientService.getDrugNames(result);
        List<TrustRankToolEdge> edges = trustRankToolClientService.getEdges(result);

        mappedResult.setDrugNames(drugName);
        mappedResult.setEdges(edges);


        return mappedResult;
    }

}
