package de.hamburg.university.api.chat.messages;

import java.util.ArrayList;
import java.util.List;

public class ChatResponseDTO {
    private List<MessageDTO> messages = new ArrayList<>();
    private MessageType messageType;
}
