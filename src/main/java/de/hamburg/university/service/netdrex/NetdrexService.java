package de.hamburg.university.service.netdrex;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
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
        return ids.stream()
                .map(this::detectPrefix)
                .map(e -> e.toString().toLowerCase())
                .filter(StringUtils::isNotEmpty)
                .flatMap(e -> api.getById(e, ids).stream())
                .toList();
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
