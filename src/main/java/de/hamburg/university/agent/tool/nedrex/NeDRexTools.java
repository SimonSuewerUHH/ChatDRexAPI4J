package de.hamburg.university.agent.tool.nedrex;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.service.nedrex.NeDRexAPIInfoDTO;
import de.hamburg.university.service.nedrex.NeDRexService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class NeDRexTools {
    @Inject
    NeDRexService neDRexService;

    @Inject
    ChatWebsocketSender chatWebsocketSender;

    @Tool("Given a list of prefixed IDs, retrieves matching entity information (e.g. drug, protein, disorder, or gene). IDs must be of the form {database}.{accession}, e.g. uniprot.Q9UBT6, drugbank.DB00001, mondo.0000030, entrez.1234. Returns an array of items with one or more matches from the appropriate collection.")
    public List<NeDRexAPIInfoDTO> getInfo(List<String> ids, @ToolMemoryId String sessionId) {
        ToolDTO toolDTO = new ToolDTO(Tools.NEDREX.name());
        toolDTO.setInput(String.join(", ", ids));

        chatWebsocketSender.sendTool(toolDTO, sessionId);
        List<NeDRexAPIInfoDTO> info = new ArrayList<>();
        try {
            info = neDRexService.fetchInfo(ids);
        } catch (Exception e) {
            Log.errorf("Error at NeDRexTool fetch", e);
        }
        toolDTO.setStop();
        toolDTO.addContent(toJson(info));
        chatWebsocketSender.sendTool(toolDTO, sessionId);
        return info;
    }

    private String toJson(List<NeDRexAPIInfoDTO> searchResults) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(searchResults);
        } catch (Exception e) {
            return "[]";
        }
    }
}
