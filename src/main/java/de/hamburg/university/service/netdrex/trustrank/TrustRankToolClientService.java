package de.hamburg.university.service.netdrex.trustrank;

import de.hamburg.university.service.netdrex.diamond.DiamondResultsDTO;
import de.hamburg.university.service.netdrex.BaseNedrexTool;
import de.hamburg.university.service.netdrex.diamond.*;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TrustRankToolClientService extends BaseNedrexTool<TrustRankApiClient, TrustRankSeedPayloadDTO, TrustRankStatusResponseDTO, TrustRankStatusResultDTO, TrustRankResultDTO> {

    @Override
    protected TrustRankResultDTO mapResult(TrustRankStatusResultDTO result) {
        return new TrustRankResultDTO();
    }

}
