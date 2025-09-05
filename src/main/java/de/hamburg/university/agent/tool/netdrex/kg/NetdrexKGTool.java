package de.hamburg.university.agent.tool.netdrex.kg;

import de.hamburg.university.agent.bot.kg.NetdrexKGBot;
import de.hamburg.university.agent.bot.kg.NetdrexKGGraph;
import de.hamburg.university.service.netdrex.kg.NetdrexKGNodeEnhanced;
import de.hamburg.university.service.netdrex.kg.NetdrexKgQueryServiceImpl;
import de.hamburg.university.service.netdrex.kg.NetdrexSearchEmbeddingsNodeDTO;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class NetdrexKGTool {


    @Inject
    NetdrexKGBot netdrexKGBot;

    @Inject
    NetdrexKgQueryServiceImpl netdrexKgQueryService;

    public NetdrexKGGraph decomposeToNodes(String question) {
        return netdrexKGBot.decomposeToNodes(question);
    }

    public String generateCypher(String question) {
        NetdrexKGGraph questionGraph = decomposeToNodes(question);
        List<NetdrexKGNodeEnhanced> enhancedNodes = netdrexKgQueryService.enhanceGraph(questionGraph);
        String enhancedNodesString = stringifyEnhancedNodes(enhancedNodes);
        String query = netdrexKGBot.generateCypherQuery(question, enhancedNodesString);
        return query;
    }

    public String answer(String question) {
        NetdrexKGGraph questionGraph = decomposeToNodes(question);
        List<NetdrexKGNodeEnhanced> enhancedNodes = netdrexKgQueryService.enhanceGraph(questionGraph);
        String enhancedNodesString = stringifyEnhancedNodes(enhancedNodes);

        for (int i = 0; i < 3; i++) {
            try {
                String query = netdrexKGBot.generateCypherQuery(question, enhancedNodesString);
                String result = netdrexKgQueryService.fireNeo4jQuery(query);
                return netdrexKGBot.answerQuestion(question, result);
            } catch (Exception e) {
                Log.warnf(e, "Attempt %d: Failed to generate answer for question: %s", i + 1, question);
            }

        }
        return "Failed to generate answer.";
    }

    private String stringifyEnhancedNodes(List<NetdrexKGNodeEnhanced> enhancedNodes) {
        StringBuilder sb = new StringBuilder();
        for (NetdrexKGNodeEnhanced node : enhancedNodes) {
            sb.append("Node Type: ").append(node.getNodeType()).append("\n");
            sb.append("Node Value: ").append(node.getNodeValue()).append("\n");
            sb.append("Sub Question: ").append(node.getSubQuestion()).append("\n");
            sb.append("Enhanced Nodes:\n");
            for (NetdrexSearchEmbeddingsNodeDTO enhancedNode : node.getNodes()) {
                sb.append("  - Primary Domain ID: ").append(enhancedNode.getPrimaryDomainId()).append("\n");
                sb.append("    Display Name: ").append(enhancedNode.getDisplayName()).append("\n");
                if (enhancedNode.getDataSources() != null) {
                    sb.append("    Data Sources: ").append(String.join(", ", enhancedNode.getDataSources())).append("\n");
                }
                if (enhancedNode.getDescription() != null) {
                    sb.append("    Description: ").append(enhancedNode.getDescription()).append("\n");
                }
                if (enhancedNode.getSynonyms() != null) {
                    sb.append("    Synonyms: ").append(String.join(", ", enhancedNode.getSynonyms())).append("\n");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
