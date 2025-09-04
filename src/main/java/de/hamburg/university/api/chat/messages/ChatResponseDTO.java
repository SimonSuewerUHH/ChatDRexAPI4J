package de.hamburg.university.api.chat.messages;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatResponseDTO {
    private List<MessageDTO> messages = new ArrayList<>();
    private MessageType messageType;


    public static ChatResponseDTO create(MessageType messageType, ChatRequestDTO request, Object result, ChatMessageType chatMessageType) {
        ChatResponseDTO response = new ChatResponseDTO();
        response.setMessageType(messageType);

        MessageDTO message = MessageDTO.createBase(request, result);
        message.setMessage(result);
        message.setMessageType(chatMessageType);

        response.getMessages().add(message);
        return response;
    }

    public static ChatResponseDTO createAPIResponse(ChatRequestDTO request, Object result) {
        return create(MessageType.RECEIVING_SINGLE, request, result, ChatMessageType.API);
    }
    public static ChatResponseDTO createToolResponse(ChatRequestDTO request, Object result) {
        return create(MessageType.RECEIVING_SINGLE, request, result, ChatMessageType.AI_TOOL);
    }

    public static ChatResponseDTO createReasoningResponse(ChatRequestDTO request, Object result) {
        return create(MessageType.RECEIVING_SINGLE, request, result, ChatMessageType.REASONING);
    }

    public static ChatResponseDTO createErrorResponse(ChatRequestDTO request, Object result) {
        return create(MessageType.RECEIVING_SINGLE, request, result, ChatMessageType.ERROR);
    }

    public static ChatResponseDTO createAIResponse(ChatRequestDTO request, Object result) {
        return create(MessageType.RECEIVING_SINGLE, request, result, ChatMessageType.AI);
    }


    public static ChatResponseDTO createSingleResponse(ChatRequestDTO request, Object result, ChatMessageType chatMessageType) {
        return create(MessageType.RECEIVING_SINGLE, request, result, chatMessageType);
    }
}
