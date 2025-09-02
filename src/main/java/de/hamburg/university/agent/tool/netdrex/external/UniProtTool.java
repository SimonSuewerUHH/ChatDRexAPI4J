package de.hamburg.university.agent.tool.netdrex.external;

import de.hamburg.university.service.uniprotkb.GeneSimpleDTO;
import de.hamburg.university.service.uniprotkb.UniProtApiClient;
import de.hamburg.university.service.uniprotkb.UniProtEntryDTO;
import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class UniProtTool {

    @Inject
    @RestClient
    UniProtApiClient uniProtApiClient;

    @Tool("Fetch gene names for a single UniProt accession ID")
    public List<String> getUniProtEntry(String uniProdId) {
        UniProtEntryDTO response = uniProtApiClient.getEntry(uniProdId);
        List<String> geneNames = new ArrayList<>();

        if (response.getGenes() != null) {
            for (GeneSimpleDTO gene : response.getGenes()) {
                if (gene.hasGeneName()) {
                    geneNames.add(gene.getGeneName().getValue());
                } else if (gene.hasPrimaryName()) {
                    geneNames.add(gene.getPrimary().getValue());
                } else {
                    Log.warnf("Unexpected structure for gene info: {}", gene);
                }
            }
        }
        return geneNames;
    }

    @Tool("Fetch gene names for multiple UniProt accession IDs")
    public List<String> getUniProtEntries(List<String> uniProdIds) {
        return uniProdIds.stream()
                .flatMap(id -> getUniProtEntry(id).stream())
                .toList();
    }
}
