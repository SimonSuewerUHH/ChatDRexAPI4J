package de.hamburg.university.api.chat;

import de.hamburg.university.agent.memory.InMemoryStateHolder;
import de.hamburg.university.agent.planning.ChatDrexAgent;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;

import java.io.Serializable;

@WebSocket(path = "/ws/{clientId}")
public class ChatWebsocketService implements Serializable {

    @Inject
    WebSocketConnection connection;

    @Inject
    ChatDrexAgent agent;

    @Inject
    ChatWebsocketSender sender;

    @Inject
    InMemoryStateHolder stateHolder;

    @OnOpen
    public void onOpen() {
        String clientId = getClientId();
        Log.info("Connection opened: " + clientId);
    }

    @OnClose
    public void onClose() {
        String clientId = getClientId();
        Log.info("Connection closed: " + clientId);
        sender.removeClient(connection.id());
        stateHolder.removeClient(connection.id());
    }

    @OnError
    public void onError(Throwable throwable) {
        String clientId = getClientId();
        Log.error("Error in WebsocketClient: " + throwable.getMessage());
        sender.removeClient(clientId);
        stateHolder.removeClient(clientId);
    }

    @OnTextMessage
    public Multi<ChatResponseDTO> stream(ChatRequestDTO request) {
        String clientId = getClientId();
        request.setConnectionId(clientId);
        sender.addClient(clientId, request);
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

    private String getClientId() {
        return connection.pathParam("clientId");
    }
}