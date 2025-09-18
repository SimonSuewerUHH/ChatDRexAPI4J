package de.hamburg.university.agent.tool;

import de.hamburg.university.agent.tool.research.ToolSourceDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ToolDTO {
    private String id;
    private String name;
    private boolean started;
    private List<String> content;
    private List<ToolStructuredContentDTO> structuredContent;
    private Object input;

    public ToolDTO(String name) {
        this.id = UUID.randomUUID().toString();
        this.started = true;
        this.name = name;
    }

    public void setStop() {
        this.started = false;
    }

    public void addContent(String content) {
        if (this.content == null) {
            this.content = new ArrayList<>();
        }
        this.content.add(content);
    }

    public void addStructuredContent(ToolStructuredContentType type, Object structuredContent) {
        if (this.structuredContent == null) {
            this.structuredContent = new ArrayList<>();
        }
        this.structuredContent.add(new ToolStructuredContentDTO(structuredContent, type));
    }

    public void addStructuredListSourceContent(ToolStructuredContentType type, List<ToolSourceDTO> structuredContent) {
        for (Object item : structuredContent) {
            addStructuredContent(type, item);
        }
    }

    public void addStructuredListFileContent(ToolStructuredContentType type, List<ToolFileResponseDTO> structuredContent) {
        for (Object item : structuredContent) {
            addStructuredContent(type, item);
        }
    }

}
