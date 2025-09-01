package de.hamburg.university.agent.workflow;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AgentResult {
    private String messageMarkdown;

    public AgentResult(String messageMarkdown) {
        this.messageMarkdown = messageMarkdown;
    }
}
