package de.hamburg.university.api.chat.messages;
import com.fasterxml.jackson.annotation.JsonAlias;

public enum ChatMessageType {
    @JsonAlias({"human", "HUMAN"})
    HUMAN,

    @JsonAlias({"system", "SYSTEM"})
    SYSTEM,

    @JsonAlias({"ai", "AI"})
    AI,

    @JsonAlias({"reasoning", "REASONING"})
    REASONING,

    @JsonAlias({"aiChunk", "AICHUNK"})
    AI_CHUNK,

    @JsonAlias({"aiTool", "AITOOL"})
    AI_TOOL,

    @JsonAlias({"research", "RESEARCH"})
    AI_RESEARCH,

    @JsonAlias({"api", "API"})
    API,

    @JsonAlias({"error", "ERROR"})
    ERROR,

    @JsonAlias({"sending", "SENDING"})
    SENDING
}