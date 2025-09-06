package de.hamburg.university.agent.memory;

import de.hamburg.university.agent.memory.bot.CompressionAIBot;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.function.Supplier;

@ApplicationScoped
public class CompleteHistoryMemoryProvider implements Supplier<ChatMemoryProvider> {

    @Inject
    ChatMemoryStore store;

    @Inject
    CompressionAIBot compressor;

    @Override
    public ChatMemoryProvider get() {
        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return new MessageWindowChatMemory.Builder().maxMessages(5).build();
            }
        };
    }
}
