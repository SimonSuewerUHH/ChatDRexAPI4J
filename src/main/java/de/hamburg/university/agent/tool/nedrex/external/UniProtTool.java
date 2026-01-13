package de.hamburg.university.agent.tool.nedrex.external;

import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.service.nedrex.NeDRexApiClient;
import de.hamburg.university.service.nedrex.NeDRexTranslateRequestDTO;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.quarkus.arc.ComponentsProvider.LOG;

@ApplicationScoped
public class UniProtTool {

    @Inject
    @RestClient
    NeDRexApiClient nedrexApiClient;

    @Inject
    ChatWebsocketSender chatWebsocketSender;

    @Tool("Given a list of gene identifiers (symbols, Entrez IDs, UniProt IDs, Ensembl IDs, etc.), retrieves the corresponding unique UniProt accession IDs (human only). Always translates ALL identifiers regardless of their current format.")
    public List<String> getUniProtIds(List<String> identifiers, @ToolMemoryId String sessionId) {
        Set<String> uniProtIds = new LinkedHashSet<>();
        ToolDTO toolDTO = new ToolDTO(Tools.NEDREX.name());

        if (identifiers == null || identifiers.isEmpty()) {
            toolDTO.setInput("");
            toolDTO.addContent("No identifiers provided.");
            toolDTO.setStop();
            chatWebsocketSender.sendTool(toolDTO, sessionId);
            return new ArrayList<>();
        }

        List<String> stringIdentifiers = identifiers.stream()
                .map(id -> id != null ? id.toString().trim() : "")
                .filter(id -> !id.isEmpty())
                .toList();

        toolDTO.setInput(String.join(", ", stringIdentifiers));
        chatWebsocketSender.sendTool(toolDTO, sessionId);

        try {
            NeDRexTranslateRequestDTO request = new NeDRexTranslateRequestDTO(stringIdentifiers);

            Map<String, List<String>> response = nedrexApiClient.translateUniProt(request);

            if (response != null) {
                for (String identifier : stringIdentifiers) {
                    toolDTO.addContent("Query:" + identifier);
                    chatWebsocketSender.sendTool(toolDTO, sessionId);

                    List<String> results = response.get(identifier);
                    if (results != null && !results.isEmpty()) {
                        String acc = results.get(0);
                        if (acc != null && !acc.trim().isEmpty()) {
                            String normalized = acc.trim();
                            if (normalized.startsWith("uniprot.")) {
                                normalized = normalized.substring("uniprot.".length());
                            }
                            uniProtIds.add(normalized);
                            toolDTO.addContent("Hit:" + normalized);
                            chatWebsocketSender.sendTool(toolDTO, sessionId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.errorf(e, "Error at calling UniProt ID translation for identifiers: %s", String.join(", ", stringIdentifiers));
        }

        toolDTO.setStop();
        chatWebsocketSender.sendTool(toolDTO, sessionId);
        return new ArrayList<>(uniProtIds);
    }

    @Tool("Given a list of Entrez Gene IDs, retrieves corresponding unique UniProt accession IDs (human only). Always translates ALL Entrez IDs regardless of their format.")
    public List<String> getUniProtIdsFromEntrez(List<String> entrezIds, @ToolMemoryId String sessionId) {
        Set<String> uniProtIds = new LinkedHashSet<>();
        ToolDTO toolDTO = new ToolDTO(Tools.NEDREX.name());

        if (entrezIds == null || entrezIds.isEmpty()) {
            toolDTO.setInput("");
            toolDTO.addContent("No Entrez IDs provided.");
            toolDTO.setStop();
            chatWebsocketSender.sendTool(toolDTO, sessionId);
            return new ArrayList<>();
        }

        List<String> stringEntrezIds = entrezIds.stream()
                .map(id -> id != null ? id.toString().trim() : "")
                .filter(id -> !id.isEmpty())
                .toList();

        toolDTO.setInput(String.join(", ", stringEntrezIds));
        chatWebsocketSender.sendTool(toolDTO, sessionId);

        try {
            NeDRexTranslateRequestDTO request = new NeDRexTranslateRequestDTO(stringEntrezIds);

            Map<String, List<String>> response = nedrexApiClient.translateUniProt(request);

            if (response != null) {
                for (String entrez : stringEntrezIds) {
                    toolDTO.addContent("Entrez:" + entrez);
                    chatWebsocketSender.sendTool(toolDTO, sessionId);

                    List<String> results = response.get(entrez);
                    if (results != null && !results.isEmpty()) {
                        String acc = results.get(0);
                        if (acc != null && !acc.trim().isEmpty()) {
                            String normalized = acc.trim();
                            if (normalized.startsWith("uniprot.")) {
                                normalized = normalized.substring("uniprot.".length());
                            }
                            uniProtIds.add(normalized);
                            toolDTO.addContent("Hit:" + normalized + " (Entrez:" + entrez + ")");
                            chatWebsocketSender.sendTool(toolDTO, sessionId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.errorf(e, "Error mapping Entrez IDs to UniProt: %s", String.join(", ", stringEntrezIds));
        }

        toolDTO.setStop();
        chatWebsocketSender.sendTool(toolDTO, sessionId);
        return new ArrayList<>(uniProtIds);
    }
}
