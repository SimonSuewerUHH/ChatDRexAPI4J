package de.hamburg.university.agent.tool.digest;

import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.service.digest.DigestApiClientService;
import de.hamburg.university.service.digest.DigestToolResultDTO;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class DigestTools {

    @Inject
    DigestApiClientService digestService;

    @Inject
    ChatWebsocketSender chatWebsocketSender;

    @Tool("Run functional enrichment (DIGEST-Set) for human genes by Entrez ID. " +
            "Use when the user says 'set enrichment', 'gene set', or does not specify. " +
            "Input: List of Entrez IDs as strings, e.g. [\"1636\",\"102\"]. " +
            "Output: DigestToolResultDTO with enrichment results."
    )
    public DigestToolResultDTO submitSet(List<String> entrez, @ToolMemoryId String sessionId) {
        ToolDTO toolDTO = new ToolDTO(Tools.DIGEST.name());
        toolDTO.setInput(String.join(", ", entrez));
        chatWebsocketSender.sendTool(toolDTO, sessionId);

        DigestToolResultDTO e = digestService.callSet(entrez).await().indefinitely();
        toolDTO.addContent("Received " + e.getRows().size() + " enrichment results from DIGEST-Set.");
        toolDTO.setStop();
        toolDTO.addStructuredContent(digestService.createPlot(e));
        chatWebsocketSender.sendTool(toolDTO, sessionId);
        return e;
    }

    @Tool("Run network-aware enrichment (DIGEST-Subnetwork) for human genes by Entrez ID. " +
            "Use when the user mentions 'subnetwork', 'module', or 'network-based' enrichment. " +
            "Input: List of Entrez IDs as strings, e.g. [\"1636\",\"102\"]. " +
            "Output: DigestToolResultDTO with enrichment results."
    )
    public DigestToolResultDTO submitSubnetwork(List<String> entrez, @ToolMemoryId String sessionId) {
        ToolDTO toolDTO = new ToolDTO(Tools.DIGEST.name());
        toolDTO.setInput(String.join(", ", entrez));
        chatWebsocketSender.sendTool(toolDTO, sessionId);

        DigestToolResultDTO e = digestService.callSubnetwork(entrez).await().indefinitely();
        toolDTO.addContent("Received " + e.getRows().size() + " enrichment results from DIGEST-Subnetwork.");
        toolDTO.setStop();
        toolDTO.addStructuredContent(digestService.createPlot(e));
        chatWebsocketSender.sendTool(toolDTO, sessionId);
        return e;
    }
}
