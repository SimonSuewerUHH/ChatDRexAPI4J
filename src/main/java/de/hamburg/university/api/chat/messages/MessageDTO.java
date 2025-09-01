package de.hamburg.university.api.chat.messages;

import lombok.Data;

import java.util.Date;

@Data
public class MessageDTO {
    private Object message;
    private ChatMessageType messageType;
    private long timestamp;
    private String messageId;
    private String threadId;

    public static MessageDTO createBase(ChatRequestDTO request, Object result){
        MessageDTO message = new MessageDTO();
        message.setMessageId(request.getMessageId());
        message.setThreadId(request.getThreadId());
        message.setTimestamp(new Date().getTime());
        message.setMessage(result);
        return message;
    }
}
