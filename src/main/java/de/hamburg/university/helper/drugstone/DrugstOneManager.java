package de.hamburg.university.helper.drugstone;

import de.hamburg.university.agent.bot.DrugstOneBot;
import de.hamburg.university.agent.memory.InMemoryStateHolder;
import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.ToolStructuredContentType;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import de.hamburg.university.helper.drugstone.dto.*;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;

@ApplicationScoped
public class DrugstOneManager {

    @Inject
    ChatWebsocketSender chatWebsocketSender;

    @Inject
    InMemoryStateHolder stateHolder;

    @Inject
    DrugstOneBot drugstOneBot;

    public DrugstOneDTO getDrugstOneDTO(String id) {
        return stateHolder.getDrugstOne(id);
    }

    public DrugstOneDTO setNetwork(ChatRequestDTO content, DrugstOneNetworkDTO network) {
        return setNetwork(content.getConnectionId(), network);
    }

    public DrugstOneDTO setNetwork(String id, DrugstOneNetworkDTO network) {
        DrugstOneDTO current = getDrugstOneDTO(id);
        current.setNetwork(network);
        addDefaultGroups(current);
        return current;
    }

    public DrugstOneDTO patchNetwork(ChatRequestDTO content, DrugstOneNetworkDTO network) {
        return patchNetwork(content.getConnectionId(), network);
    }

    public DrugstOneDTO patchNetwork(String id, DrugstOneNetworkDTO network) {
        DrugstOneDTO current = getDrugstOneDTO(id);
        current.getNetwork().patch(network);
        addDefaultGroups(current);
        return current;
    }

    public DrugstOneNetworkDTO getNetwork(String id) {
        DrugstOneDTO current = getDrugstOneDTO(id);
        return current.getNetwork();
    }

    public DrugstOneDTO setConfig(String id, DrugstOneConfigDTO config) {
        DrugstOneDTO current = getDrugstOneDTO(id);
        current.setConfig(config);
        return current;
    }

    public DrugstOneDTO setGroups(String id, DrugstOneGroupsConfigDTO config) {
        DrugstOneDTO current = getDrugstOneDTO(id);
        current.setGroups(config);
        return current;
    }

    public DrugstOneConfigDTO getConfig(String id) {
        DrugstOneDTO current = getDrugstOneDTO(id);
        return current.getConfig();
    }

    public DrugstOneGroupsConfigDTO getGroups(String id) {
        DrugstOneDTO current = getDrugstOneDTO(id);
        return current.getGroups();
    }


    public void send(ToolDTO tool, ChatRequestDTO content, MultiEmitter<? super ChatResponseDTO> emitter) {
        DrugstOneDTO current = getDrugstOneDTO(content.getConnectionId());
        tool.addStructuredContent(ToolStructuredContentType.DRUGST_ONE, current);
        chatWebsocketSender.sendTool(tool, content, emitter);
    }

    public void send(ToolDTO tool, String sessionId) {
        DrugstOneDTO current = getDrugstOneDTO(sessionId);
        tool.addStructuredContent(ToolStructuredContentType.DRUGST_ONE, current);
        chatWebsocketSender.sendTool(tool, sessionId);
    }

    public void stopAndSend(ToolDTO tool, ChatRequestDTO content, MultiEmitter<? super ChatResponseDTO> emitter) {
        tool.setStop();
        send(tool, content, emitter);
    }

    public void stopAndSend(ToolDTO tool, String sessionId) {
        tool.setStop();
        send(tool, sessionId);
    }

    public String analyzeNetwork(String question, ChatRequestDTO content) {
        return analyzeNetwork(question, content.getConnectionId());
    }

    public String analyzeNetwork(String question, String sessionId) {
        ToolDTO tool = new ToolDTO(Tools.DRUGST_ONE.name());
        tool.setInput("Analyzing Drugst.One network with question: " + question);
        chatWebsocketSender.sendTool(tool, sessionId);
        DrugstOneNetworkDTO current = getNetwork(sessionId);

        String answer = drugstOneBot.answer(question, current);
        tool.addContent("Answer: " + answer);
        tool.setStop();
        chatWebsocketSender.sendTool(tool, sessionId);
        return answer;
    }

    public void addDefaultGroups(DrugstOneDTO current) {
        if (current.getGroups() == null) {
            current.setGroups(new DrugstOneGroupsConfigDTO());
        }

        Map<String, DrugstOneGroupsConfigNodeDTO> nodeGroups = current.getGroups().getNodeGroups();
        if (nodeGroups == null) {
            nodeGroups = new LinkedHashMap<>();
        }
        List<DrugstOneNodeDTO> nodes = current.getNetwork().getNodes();
        if (nodes == null || nodes.isEmpty()) {
            current.getGroups().setNodeGroups(nodeGroups);
            return;
        }
        Set<String> existingTypes = new HashSet<>();
        for(DrugstOneNodeDTO node : current.getNetwork().getNodes()) {
            String nodeType = node.getGroup();
            if(nodeType == null) {
                continue;
            }
            if(!existingTypes.contains(nodeType)) {
                existingTypes.add(nodeType);
            }else{
                continue;
            }
            nodeGroups.put(nodeType,DrugstOneNodeGroupDefaults.getByName(nodeType));
        }
        for(String group : nodeGroups.keySet()) {
            if(!existingTypes.contains(group)) {
                nodeGroups.remove(group);
            }
        }
        current.getGroups().setNodeGroups(nodeGroups);

        if (current.getGroups().getEdgeGroups() == null || current.getGroups().getEdgeGroups().isEmpty()) {
            Map<String, DrugstOneGroupsConfigEdgeDTO> defaultEdgeGroups = new LinkedHashMap<>();
            defaultEdgeGroups.put("default", DrugstOneNodeGroupDefaults.defaultEdge);
            current.getGroups().setEdgeGroups(defaultEdgeGroups);
        }

    }

}
