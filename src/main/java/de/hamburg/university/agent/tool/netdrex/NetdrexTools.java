package de.hamburg.university.agent.tool.netdrex;

import de.hamburg.university.service.netdrex.NetdrexAPIInfoDTO;
import de.hamburg.university.service.netdrex.NetdrexService;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class NetdrexTools {
    @Inject
    NetdrexService netdrexService;

    @Tool("Given a list of prefixed IDs, retrieves matching entity information (drug, protein, or gene). IDs must be of the form {database}.{accession}, e.g. uniprot.Q9UBT6, drugbank.DB00001, entrez.1234. Returns an array of items with one or more matches from the appropriate collection.")
    public List<NetdrexAPIInfoDTO> getInfo(List<String> ids) {
        return netdrexService.fetchInfo(ids);
    }
}
