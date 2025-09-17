package de.hamburg.university.api.chat;

import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.OpenConnections;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ChatWebsocketSender {
    private static final String ENDPOINT_ID = ChatWebsocketService.class.getName();

    @Inject
    OpenConnections connections;

    private final ConcurrentHashMap<Object, ChatRequestDTO> currentClients = new ConcurrentHashMap<>();

    public void addClient(Object clientId, ChatRequestDTO request) {
        currentClients.put(clientId, request);
    }

    public void removeClient(Object clientId) {
        currentClients.remove(clientId);
    }


    public void sendTool(ToolDTO tool, Object memoryId) {
        if (!currentClients.containsKey(memoryId)) {
            Log.warnf("No client found with id %s", memoryId);
            return;
        }
        ChatRequestDTO request = currentClients.get(memoryId);
        ChatResponseDTO start = ChatResponseDTO.createToolResponse(request, tool);
        sendMessageToClient(start, request.getConnectionId());
    }


    public void sendTool(ToolDTO tool, ChatRequestDTO content, MultiEmitter<? super ChatResponseDTO> emitter) {
        if (emitter == null) {
            Log.warnf("No emitter found for message %s", tool.getId());
            return;
        }
        ChatResponseDTO start = ChatResponseDTO.createToolResponse(content, tool);
        emitter.emit(start);
    }

    private void sendMessageToClient(ChatResponseDTO message, String clientId) {
        connections.findByEndpointId(ENDPOINT_ID)
                .stream()
                .filter(c -> c.pathParam("clientId").equals(clientId))
                .forEach(c -> {
                    try {
                        c.sendTextAndAwait(message);
                    } catch (Exception e) {
                        Log.errorf("Failed to send message to connection %s: %s", c.id(), e.getMessage());
                    }
                });
    }
}
