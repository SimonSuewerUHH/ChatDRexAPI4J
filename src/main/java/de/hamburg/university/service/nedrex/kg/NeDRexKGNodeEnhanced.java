package de.hamburg.university.service.nedrex.kg;

import de.hamburg.university.agent.bot.kg.NeDRexKGNode;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ApplicationScoped
@NoArgsConstructor
public class NeDRexKGNodeEnhanced extends NeDRexKGNode {
    private List<NeDRexSearchEmbeddingsNodeDTO> nodes;

    public NeDRexKGNodeEnhanced(NeDRexKGNode node, List<NeDRexSearchEmbeddingsNodeDTO> nodes) {
        this.setNodeType(node.getNodeType());
        this.setNodeValue(node.getNodeValue());
        this.setSubQuestion(node.getSubQuestion());
        this.setNodes(nodes);
    }
}
