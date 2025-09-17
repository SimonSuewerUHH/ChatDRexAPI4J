package de.hamburg.university.agent.tool.netdrex;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.service.netdrex.NetdrexAPIInfoDTO;
import de.hamburg.university.service.netdrex.NetdrexService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class NetdrexTools {
    @Inject
    NetdrexService netdrexService;

    @Inject
    ChatWebsocketSender chatWebsocketSender;

    @Tool("Given a list of prefixed IDs, retrieves matching entity information (drug, protein, or gene). IDs must be of the form {database}.{accession}, e.g. uniprot.Q9UBT6, drugbank.DB00001, entrez.1234. Returns an array of items with one or more matches from the appropriate collection.")
    public List<NetdrexAPIInfoDTO> getInfo(List<String> ids, @ToolMemoryId String sessionId) {
        ToolDTO toolDTO = new ToolDTO(Tools.NETDREX.name());
        toolDTO.setInput(String.join(", ", ids));

        chatWebsocketSender.sendTool(toolDTO, sessionId);
        List<NetdrexAPIInfoDTO> info = new ArrayList<>();
        try {
            info = netdrexService.fetchInfo(ids);
        } catch (Exception e) {
            Log.errorf("Error at NetdrexTool fetch", e);
        }
        toolDTO.setStop();
        toolDTO.addContent(toJson(info));
        chatWebsocketSender.sendTool(toolDTO, sessionId);
        return info;
    }

    private String toJson(List<NetdrexAPIInfoDTO> searchResults) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(searchResults);
        } catch (Exception e) {
            return "[]";
        }
    }
}
