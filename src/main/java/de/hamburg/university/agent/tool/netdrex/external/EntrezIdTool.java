package de.hamburg.university.agent.tool.netdrex.external;

import de.hamburg.university.service.mygene.MyGeneClient;
import de.hamburg.university.service.mygene.MyGeneHit;
import de.hamburg.university.service.mygene.MyGeneResponseDTO;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.quarkus.arc.ComponentsProvider.LOG;

@ApplicationScoped
public class EntrezIdTool {

    @Inject
    @RestClient
    MyGeneClient myGeneClient;


    @Tool("Given a list of gene symbols, retrieves the corresponding unique Entrez Gene IDs (human only).")
    public List<Integer> getEntrezIds(List<String> genes) {
        Set<Integer> entrezIds = new HashSet<>();
        for (String gene : genes) {
            try {
                MyGeneResponseDTO response = myGeneClient.query(gene, "entrezgene", "human");
                if (response != null && response.getHits() != null && !response.getHits().isEmpty()) {
                    MyGeneHit hit = response.getHits().get(0);
                    if (hit != null && hit.getEntrezgene() != null) {
                        entrezIds.add(hit.getEntrezgene());
                    }
                }
            } catch (Exception e) {
                LOG.errorf(e, "Error at calling Entrez-ID by gene '%s'", gene);
            }
        }
        return new ArrayList<>(entrezIds);
    }
}
