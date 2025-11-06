package de.hamburg.university.service.nedrex.trustrank;

import de.hamburg.university.service.nedrex.BaseNedrexTool;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TrustRankToolClientService extends BaseNedrexTool<TrustRankApiClient, TrustRankSeedPayloadDTO, TrustRankStatusResponseDTO, TrustRankStatusResultDTO, TrustRankResultDTO> {

    @Override
    protected TrustRankResultDTO mapResult(TrustRankStatusResultDTO result) {
        TrustRankResultDTO mappedResult = new TrustRankResultDTO();

        List<String> drugName = getDrugNames(result);
        List<TrustRankToolEdge> edges = getEdges(result);

        mappedResult.setDrugNames(drugName);
        mappedResult.setEdges(edges);

        return mappedResult;
    }


    public List<String> getDrugNames(TrustRankStatusResultDTO result) {
        return result.getDrugs().stream()
                .map(TrustrankNodeDTO::getDrugName)
                .toList();
    }

    public List<TrustRankToolEdge> getEdges(TrustRankStatusResultDTO result) {
        return result.getEdges().stream()
                .map(e -> {
                    TrustRankToolEdge edge = new TrustRankToolEdge();
                    edge.setFrom(e.get(0));
                    edge.setTo(e.get(1));
                    return edge;
                })
                .toList();
    }
}
