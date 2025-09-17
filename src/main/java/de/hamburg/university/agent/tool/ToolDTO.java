package de.hamburg.university.agent.tool;

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
    private List<Object> structuredContent;
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

    public void addStructuredContent(Object structuredContent) {
        if (this.structuredContent == null) {
            this.structuredContent = new ArrayList<>();
        }
        this.structuredContent.add(structuredContent);
    }
}
