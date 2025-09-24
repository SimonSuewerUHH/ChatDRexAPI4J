package de.hamburg.university.socket;

import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

@ApplicationScoped
@Priority(1)
@Alternative
public class TestChatWebsocketSender extends ChatWebsocketSender {


    @Override
    public void sendTool(ToolDTO tool, Object memoryId) {
        //IGNORE FOR NOW
    }

    @Override
    public void sendTool(ToolDTO tool, ChatRequestDTO content, MultiEmitter<? super ChatResponseDTO> emitter) {
        //IGNORE FOR NOW
    }

    @Override
    protected void sendMessageToClient(ChatResponseDTO message, String clientId) {
        //IGNORE FOR NOW
    }
}
