package de.hamburg.university.agent.tool.nedrex.external;

import de.hamburg.university.agent.tool.ToolDTO;
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
        ToolDTO toolDTO = new ToolDTO(Tools.NEDREX.name());
        toolDTO.setInput(String.join(", ", genes));
        chatWebsocketSender.sendTool(toolDTO, sessionId);

        for (String gene : genes) {
            try {
                toolDTO.addContent("Query:" + gene);
                chatWebsocketSender.sendTool(toolDTO, sessionId);
                MyGeneResponseDTO response = myGeneClient.query(gene, "entrezgene", "human");
                if (response != null && response.getHits() != null && !response.getHits().isEmpty()) {
                    for (MyGeneHit hit : response.getHits()) {
                        if (hit.getEntrezgene() != null) {
                            entrezIds.add(hit.getEntrezgene());
                            toolDTO.addContent("Hit:" + hit.getEntrezgene());
                            chatWebsocketSender.sendTool(toolDTO, sessionId);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.errorf(e, "Error at calling Entrez ID by gene '%s'", gene);
            }
        }
        toolDTO.setStop();
        chatWebsocketSender.sendTool(toolDTO, sessionId);
        return new ArrayList<>(entrezIds);
    }
}
