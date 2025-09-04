package de.hamburg.university.api.chat;

import de.hamburg.university.agent.workflow.ChatDrexAgent;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;

@WebSocket(path = "/ws/{clientId}")
public class ChatWebsocketService {

    @Inject
    WebSocketConnection connection;

    @Inject
    ChatDrexAgent agent;

    @Inject
    ChatWebsocketSender sender;

    @OnOpen
    public void onOpen() {
        Log.info("Connection opened: " + connection.id());
    }

    @OnClose
    public void onClose() {
        Log.info("Connection closed: " + connection.id());
        sender.removeClient(connection.id());
    }

    @OnError
    public void onError(Throwable throwable) {
        Log.error("Error in WebsocketClient: " + throwable.getMessage());
        sender.removeClient(connection.id());
    }

    @OnTextMessage
    public Multi<ChatResponseDTO> stream(ChatRequestDTO request) {
        sender.addClient(connection.id(), request);
        ChatResponseDTO start = ChatResponseDTO.createAPIResponse(request, "Start");
        ChatResponseDTO stop = ChatResponseDTO.createAPIResponse(request, "Stop");

        Multi<ChatResponseDTO> core = agent.answer(request)
                .onFailure().recoverWithItem(t -> {
                    Log.error("answer() failed", t);
                    return ChatResponseDTO.createErrorResponse(request, t.getMessage());
                });

        return Multi.createBy().concatenating().streams(
                Multi.createFrom().item(start),
                core,
                Multi.createFrom().item(stop)
        ).runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }
}