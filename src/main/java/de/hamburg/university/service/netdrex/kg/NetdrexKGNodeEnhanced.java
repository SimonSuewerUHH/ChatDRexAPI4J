package de.hamburg.university.service.netdrex.kg;

import de.hamburg.university.agent.bot.kg.NetdrexKGNode;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ApplicationScoped
@NoArgsConstructor
public class NetdrexKGNodeEnhanced extends NetdrexKGNode {
    private List<NetdrexSearchEmbeddingsNodeDTO> nodes;

    public NetdrexKGNodeEnhanced(NetdrexKGNode node, List<NetdrexSearchEmbeddingsNodeDTO> nodes) {
        this.setNodeType(node.getNodeType());
        this.setNodeValue(node.getNodeValue());
        this.setSubQuestion(node.getSubQuestion());
        this.setNodes(nodes);
    }
}
