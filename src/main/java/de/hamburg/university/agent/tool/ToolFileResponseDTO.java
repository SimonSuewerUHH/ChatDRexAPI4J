package de.hamburg.university.agent.tool;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolFileResponseDTO {
    private String path;
    private String name;
    private ToolFileResponseType type;
}
