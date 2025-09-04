package de.hamburg.university.api.chat;

import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.OpenConnections;
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

    private void sendToolResponse(String message, Object memoryId, String prefix) {
        if (!currentClients.containsKey(memoryId)) {
            Log.warnf("No client found with id %s", memoryId);
            return;
        }
        ChatRequestDTO request = currentClients.get(memoryId);
        ChatResponseDTO start = ChatResponseDTO.createToolResponse(request, prefix + message);
        sendMessageToClient(start, memoryId.toString());
    }

    public void sendToolStartResponse(String message, Object memoryId) {
        sendToolResponse(message, memoryId, "START");
    }

    public void sendToolResponse(String message, Object memoryId) {
        sendToolResponse(message, memoryId, "CONTENT");
    }

    public void sendToolStopResponse(String message, Object memoryId) {
        sendToolResponse(message, memoryId, "END");
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
