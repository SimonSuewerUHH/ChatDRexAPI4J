package de.hamburg.university.agent.bot.kg;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

import java.util.List;

@Data
public class NeDRexKGGraph {
    @Description("List of nodes")
    private List<NeDRexKGNode> nodes;
}
