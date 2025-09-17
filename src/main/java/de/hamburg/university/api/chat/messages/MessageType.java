package de.hamburg.university.api.chat.messages;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum MessageType {
    @JsonAlias({"sending", "SENDING"})
    SENDING,

    @JsonAlias({"get_all", "GET_ALL"})
    GET_ALL,

    @JsonAlias({"receiving:error", "RECEIVING_ERROR"})
    RECEIVING_ERROR,

    @JsonAlias({"receiving:single", "RECEIVING_SINGLE"})
    RECEIVING_SINGLE,

    @JsonAlias({"receiving:system", "RECEIVING_SYSTEM"})
    RECEIVING_SYSTEM,

    @JsonAlias({"receiving:all", "RECEIVING_ALL"})
    RECEIVING_ALL
}

