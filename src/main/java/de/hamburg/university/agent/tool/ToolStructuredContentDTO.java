package de.hamburg.university.agent.tool;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolStructuredContentDTO {
    private Object content;
    private ToolStructuredContentType type;
}
