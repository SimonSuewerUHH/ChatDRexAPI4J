package de.hamburg.university.api.chat;

import de.hamburg.university.agent.memory.InMemoryStateHolder;
import de.hamburg.university.agent.planning.ChatDrexAgent;
import de.hamburg.university.agent.provider.setting.UserLLMModelSettingDTO;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Flow.Subscription;

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

    private volatile Subscription currentStream;

    @OnOpen
    public void onOpen() {
        try {
            String clientId = getClientId();
            Log.info("Connection opened: " + clientId);

        } catch (Exception e) {
            Log.error("Error in onOpen: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose() {
        try {
            String clientId = getClientId();
            Log.info("Connection closed: " + clientId);
            sender.removeClient(connection.id());
            stateHolder.removeClient(connection.id());

            if (currentStream != null) {
                currentStream.cancel();
            }
        } catch (Exception e) {
            Log.error("Error in onClose: " + e.getMessage());
        }
    }

    @OnError
    public void onError(Throwable throwable) {
        try {
            String clientId = getClientId();
            Log.error("Error in WebsocketClient: " + throwable.getMessage());
            sender.removeClient(clientId);
            stateHolder.removeClient(clientId);
        } catch (Exception e) {
            Log.error("Error in onError: " + e.getMessage());
        }
    }

    @OnTextMessage
    public Multi<ChatResponseDTO> stream(ChatRequestDTO request) {
        Map<String, String> queryParams = splitQuery(connection.handshakeRequest().query());
        String base64Setting = queryParams.get("llmsetting");
        UserLLMModelSettingDTO settings = UserLLMModelSettingDTO.getFromString(base64Setting);
        String clientId = getClientId();
        request.setConnectionId(clientId);
        sender.addClient(clientId, request);
        ChatResponseDTO start = ChatResponseDTO.createAPIResponse(request, "Start");
        ChatResponseDTO stop = ChatResponseDTO.createAPIResponse(request, "Stop");

        Multi<ChatResponseDTO> core = agent.answer(request, settings)
                .onSubscription().invoke(sub -> this.currentStream = sub)
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

    public static Map<String, String> splitQuery(String query) {
        if (StringUtils.isEmpty(query)) {
            return Map.of();
        }
        final Map<String, String> queryPairs = new LinkedHashMap<>();
        final String[] pairs = query.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : null;
            queryPairs.put(key, value);
        }
        return queryPairs;
    }
}