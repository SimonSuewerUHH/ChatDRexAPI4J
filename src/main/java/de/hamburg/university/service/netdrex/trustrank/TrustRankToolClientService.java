package de.hamburg.university.service.netdrex.trustrank;

import de.hamburg.university.service.netdrex.BaseNedrexTool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TrustRankToolClientService extends BaseNedrexTool<TrustRankApiClient, TrustRankSeedPayloadDTO, TrustRankStatusResponseDTO, TrustRankStatusResultDTO, TrustRankResultDTO> {

    @Override
    protected TrustRankResultDTO mapResult(TrustRankStatusResultDTO result) {
        return new TrustRankResultDTO();
    }

}
