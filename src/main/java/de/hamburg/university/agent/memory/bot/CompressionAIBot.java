package de.hamburg.university.agent.memory.bot;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
public interface CompressionAIBot {
    @SystemMessage("""
            You compress dialogue into a *single compact line* that preserves the user’s original intent and the assistant’s core answer.
            Output format:
            "Q: <short user intent> | A: <short core answer>"
            Keep it under ~200 chars, no extra commentary.
            """)
    @UserMessage("""
            {originalQuestion}
            
            Additional input context
            {assistantAnswer}
            """)
    String compress(@V("originalQuestion") String originalQuestion, @V("assistantAnswer") String assistantAnswer);
}
