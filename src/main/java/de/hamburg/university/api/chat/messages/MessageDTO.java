package de.hamburg.university.api.chat.messages;

import lombok.Data;

@Data
public class MessageDTO {
    private Object message;
    private ChatMessageType messageType;
    private long timestamp;
    private String messageId;
    private String threadId;
}
