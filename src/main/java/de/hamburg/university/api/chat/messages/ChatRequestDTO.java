package de.hamburg.university.api.chat.messages;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRequestDTO {

    private String token;

    @NotBlank
    private String threadId;

    @NotBlank
    private String messageId;

    @NotBlank
    private String message;

    @NotNull
    private ChatMessageType messageType;

    @NotNull
    @PositiveOrZero
    private Long timestamp;

    //Will be set internally when using WebSocket connections
    private String connectionId;
}