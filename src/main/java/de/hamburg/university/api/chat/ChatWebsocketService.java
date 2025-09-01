package de.hamburg.university.api.chat;

import de.hamburg.university.agent.workflow.ChatDrexAgent;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.*;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;

@WebSocket(path = "/ws/{clientId}")
public class ChatWebsocketService {

    @Inject
    WebSocketConnection connection;

    @Inject
    ChatDrexAgent agent;

    @OnOpen
    public void onOpen() {
        Log.info("Connection opened: " + connection.id());
    }

    @OnClose
    public void onClose() {
        Log.info("Connection closed: " + connection.id());
    }

    @OnError
    public void onError(Throwable throwable) {
        Log.error("Error in WebsocketClient: " + throwable.getMessage());
    }

    @OnTextMessage
    public Multi<ChatResponseDTO> stream(ChatRequestDTO request) {
        return agent.answer(request);
    }
}