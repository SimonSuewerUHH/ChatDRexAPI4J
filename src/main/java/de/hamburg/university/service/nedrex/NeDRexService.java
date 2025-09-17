package de.hamburg.university.service.nedrex;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class NeDRexService {

    @Inject
    @RestClient
    protected NeDRexApiClient api;

    public List<NeDRexAPIInfoDTO> fetchInfo(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .map(this::detectPrefix)
                .map(e -> e.toString().toLowerCase())
                .filter(StringUtils::isNotEmpty)
                .flatMap(e -> api.getByIds(new NeDRexAPIInfoRequestDTO(e, ids)).stream())
                .toList();
    }

    public NeDRexAPIInfoDTO fetchSingleInfo(String id) {
        String prefix = detectPrefix(id).toString().toLowerCase();
        List<NeDRexAPIInfoDTO> result = api.getById(prefix, id);
        if (result == null || result.isEmpty()) {
            throw new NotFoundException("No NeDRex API found for id: " + id);
        }
        return result.getFirst();
    }

    public List<NeDRexAPIInfoDTO> fetchInfo(String id) {
        return fetchInfo(List.of(id));
    }

    private NeDRexNodeCollection detectPrefix(String id) {
        if (id.startsWith("drugbank.")) {
            return NeDRexNodeCollection.DRUG;
        } else if (id.startsWith("uniprot.")) {
            return NeDRexNodeCollection.PROTEIN;
        } else if (id.startsWith("entrez.")) {
            return NeDRexNodeCollection.GENE;
        } else {
            return NeDRexNodeCollection.DRUG;
        }
    }

}
