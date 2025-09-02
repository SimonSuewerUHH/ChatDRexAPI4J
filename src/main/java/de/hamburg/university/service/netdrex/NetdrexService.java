package de.hamburg.university.service.netdrex;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class NetdrexService {

    @Inject
    @RestClient
    protected NetdrexApiClient api;

    public List<NetdrexAPIInfoDTO> fetchInfo(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String first = ids.getFirst();
        NetdrexNodeCollection entityType = detectPrefix(first);

        return api.getById(entityType.toString().toLowerCase(), ids);
    }

    private NetdrexNodeCollection detectPrefix(String id) {
        if (id.startsWith("drugbank.")) {
            return NetdrexNodeCollection.DRUG;
        } else if (id.startsWith("uniprot.")) {
            return NetdrexNodeCollection.PROTEIN;
        } else if (id.startsWith("entrez.")) {
            return NetdrexNodeCollection.GENE;
        } else {
            return NetdrexNodeCollection.DRUG;
        }
    }

}
