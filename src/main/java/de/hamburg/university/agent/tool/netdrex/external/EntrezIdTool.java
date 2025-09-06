package de.hamburg.university.agent.tool.netdrex.external;

import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.service.mygene.MyGeneClient;
import de.hamburg.university.service.mygene.MyGeneHit;
import de.hamburg.university.service.mygene.MyGeneResponseDTO;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
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
    @Inject
    ChatWebsocketSender chatWebsocketSender;


    @Tool("Given a list of gene symbols, retrieves the corresponding unique Entrez Gene IDs (human only).")
    public List<Integer> getEntrezIds(List<String> genes, @ToolMemoryId String sessionId) {
        Set<Integer> entrezIds = new HashSet<>();
        chatWebsocketSender.sendToolStartResponse(Tools.MYGENE.name(), sessionId);

        for (String gene : genes) {
            try {
                chatWebsocketSender.sendToolResponse("Query:" + gene, sessionId);
                MyGeneResponseDTO response = myGeneClient.query(gene, "entrezgene", "human");
                if (response != null && response.getHits() != null && !response.getHits().isEmpty()) {
                    for (MyGeneHit hit : response.getHits()) {
                        if (hit.getEntrezgene() != null) {
                            entrezIds.add(hit.getEntrezgene());
                            chatWebsocketSender.sendToolResponse("Hit:" + hit.getEntrezgene(), sessionId);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.errorf(e, "Error at calling Entrez-ID by gene '%s'", gene);
            }
        }
        chatWebsocketSender.sendToolStopResponse(Tools.MYGENE.name(), sessionId);
        return new ArrayList<>(entrezIds);
    }
}
