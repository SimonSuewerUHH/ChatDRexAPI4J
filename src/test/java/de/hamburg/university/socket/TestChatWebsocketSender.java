package de.hamburg.university.socket;

import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.ToolStructuredContentDTO;
import de.hamburg.university.agent.tool.ToolStructuredContentType;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
@Priority(1)
@Alternative
public class TestChatWebsocketSender extends ChatWebsocketSender {

    private List<ToolDTO> tools = new ArrayList<>();

    @Override
    public void sendTool(ToolDTO tool, Object memoryId) {
        tools.add(tool);
    }

    @Override
    public void sendTool(ToolDTO tool, ChatRequestDTO content, MultiEmitter<? super ChatResponseDTO> emitter) {
        tools.add(tool);
    }

    @Override
    protected void sendMessageToClient(ChatResponseDTO message, String clientId) {
        //IGNORE FOR NOW
    }

    public <T> List<T> findContentByToolAndContentType(Tools toolName, ToolStructuredContentType contentType) {
        return tools.stream()
                .filter(tool -> tool.getName().equals(toolName.name()))
                .flatMap(tool -> tool.getStructuredContent() != null ? tool.getStructuredContent().stream() : Stream.empty())
                .filter(content -> content.getType().equals(contentType))
                .map(content -> (T) content.getContent())
                .collect(Collectors.toList());
    }

    public void clearTools() {
        if(tools == null) {
            tools = new ArrayList<>();
        }
        tools.clear();
    }
}
