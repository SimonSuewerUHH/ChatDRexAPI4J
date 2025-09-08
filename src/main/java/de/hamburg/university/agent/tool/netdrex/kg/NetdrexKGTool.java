package de.hamburg.university.agent.tool.netdrex.kg;

import de.hamburg.university.agent.bot.kg.NetdrexKGBot;
import de.hamburg.university.agent.bot.kg.NetdrexKGGraph;
import de.hamburg.university.agent.bot.kg.NetdrexKGNode;
import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import de.hamburg.university.service.netdrex.kg.NetdrexKGNodeEnhanced;
import de.hamburg.university.service.netdrex.kg.NetdrexKgQueryServiceImpl;
import de.hamburg.university.service.netdrex.kg.NetdrexSearchEmbeddingsNodeDTO;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@ApplicationScoped
public class NetdrexKGTool {

    @Inject
    ChatWebsocketSender chatWebsocketSender;

    @Inject
    NetdrexKGBot netdrexKGBot;

    @Inject
    NetdrexKgQueryServiceImpl netdrexKgQueryService;

    public NetdrexKGGraph decomposeToNodes(String question) {
        return decomposeToNodes(question, "");
    }

    public NetdrexKGGraph decomposeToNodes(String question, String context) {
        return netdrexKGBot.decomposeToNodes(question, context);
    }

    public String generateCypher(String question) {
        NetdrexKGGraph questionGraph = decomposeToNodes(question);
        List<NetdrexKGNodeEnhanced> enhancedNodes = netdrexKgQueryService.enhanceGraph(questionGraph);
        String enhancedNodesString = stringifyEnhancedNodes(enhancedNodes);
        String query = netdrexKGBot.generateCypherQuery(question, enhancedNodesString, "");
        return query;
    }

    public String answer(String question, String context, ChatRequestDTO content, MultiEmitter<? super ChatResponseDTO> emitter) {
        ToolDTO toolDTO = new ToolDTO(Tools.NETDREX_KG.name());
        toolDTO.setInput(question);

        try {
            chatWebsocketSender.sendTool(toolDTO, content, emitter);

            NetdrexKGGraph questionGraph = decomposeToNodes(question, context);
            toolDTO.addContent("<h3>Decomposed Nodes:</h3>");
            toolDTO.addContent(stringifyNodesToHtml(questionGraph.getNodes()));
            chatWebsocketSender.sendTool(toolDTO, content, emitter);
            List<NetdrexKGNodeEnhanced> enhancedNodes = netdrexKgQueryService.enhanceGraph(questionGraph);
            String enhancedNodesString = stringifyEnhancedNodesToHTML(enhancedNodes);
            toolDTO.addContent("<h3>Enhanced Nodes:</h3>");
            toolDTO.addContent(enhancedNodesString);
            chatWebsocketSender.sendTool(toolDTO, content, emitter);

            String oldQuery = "";
            final int maxAttempts = 3;
            for (int i = 0; i < maxAttempts; i++) {
                try {
                    String query = netdrexKGBot.generateCypherQuery(question, enhancedNodesString, oldQuery);
                    oldQuery += "\n " + i + ". " + query;
                    toolDTO.addContent("<h3>Neo4j Query:</h3>");
                    toolDTO.addContent(query);
                    chatWebsocketSender.sendTool(toolDTO, content, emitter);

                    String result = netdrexKgQueryService.fireNeo4jQuery(query);
                    toolDTO.addContent("<h3>Neo4j Result:</h3>");
                    toolDTO.addContent(result);
                    chatWebsocketSender.sendTool(toolDTO, content, emitter);

                    Log.infof("Generated Cypher Query: \n%s\nfor question %s", query, question);
                    if (StringUtils.isEmpty(result)) {
                        Log.infof("Empty result for query: %s", query);
                        continue;
                    }
                    if (i == maxAttempts - 1) {
                        Log.infof("Final attempt %d, returning result even if it might be incomplete.", i + 1);
                        break;
                    }
                    String answer = netdrexKGBot.answerQuestion(question, result);
                    toolDTO.addContent(answer);
                    toolDTO.setStop();
                    chatWebsocketSender.sendTool(toolDTO, content, emitter);

                    return answer;
                } catch (Exception e) {
                    Log.warnf(e, "Attempt %d: Failed to generate answer for question: %s", i + 1, question);
                }

            }
            List<NetdrexKGNodeEnhanced> enhancedNodesFallback = netdrexKgQueryService.enhanceFallbackNodes(enhancedNodes);
            String enhancedNodesFallbackString = stringifyEnhancedNodes(enhancedNodesFallback);
            toolDTO.addContent("<h3>Query Failed => Fallback</h3>");
            toolDTO.addContent("<h3>Enhanced Nodes Fallback:</h3>");
            toolDTO.addContent(enhancedNodesString);
            chatWebsocketSender.sendTool(toolDTO, content, emitter);

            String answer = netdrexKGBot.answerFallbackQuestion(question, enhancedNodesFallbackString);
            toolDTO.addContent(answer);
            toolDTO.setStop();
            chatWebsocketSender.sendTool(toolDTO, content, emitter);
            return answer;
        } catch (Exception e) {
            Log.errorf(e, "Failed to answer question: %s", question);
            String answer = "I'm sorry, I encountered an error while trying to answer your question.";
            toolDTO.addContent(answer);
            toolDTO.setStop();
            chatWebsocketSender.sendTool(toolDTO, content, emitter);

            return answer;
        }
    }

    public String answer(String question) {
        return answer(question, "", null, null);
    }

    private String stringifyNodesToHtml(List<NetdrexKGNode> enhancedNodes) {
        StringBuilder sb = new StringBuilder();
        for (NetdrexKGNode node : enhancedNodes) {
            sb.append("<b>Node Type:</b> ").append(node.getNodeType()).append("<br>");
            sb.append("<b>Node Value:</b> ").append(node.getNodeValue()).append("<br>");
            sb.append("<b>Sub Question:</b> ").append(node.getSubQuestion()).append("<br>");
            sb.append("<b>Needs Filter:</b> ").append(node.getNeedsFilter()).append("<br>");
            sb.append("<br>");
        }
        return sb.toString();
    }
    private String stringifyEnhancedNodesToHTML(List<NetdrexKGNodeEnhanced> enhancedNodes) {
        StringBuilder sb = new StringBuilder();
        for (NetdrexKGNodeEnhanced node : enhancedNodes) {
            sb.append("<b>Node Type:</b> ").append(node.getNodeType()).append("<br>");
            sb.append("<b>Node Value:</b> ").append(node.getNodeValue()).append("<br>");
            sb.append("<b>Sub Question:</b> ").append(node.getSubQuestion()).append("<br>");
            sb.append("<b>Needs Filter:</b> ").append(node.getNeedsFilter()).append("<br>");
            if (node.getNeedsFilter() != null && !node.getNeedsFilter()) {
                sb.append("  - No filtering applied.<br><br>");
                continue;
            }
            sb.append("<b>Enhanced Nodes:</b><br>");
            for (NetdrexSearchEmbeddingsNodeDTO enhancedNode : node.getNodes()) {
                sb.append("  - <b>Primary Domain ID:</b> ").append(enhancedNode.getPrimaryDomainId()).append("<br>");
                sb.append("    <b>Display Name:</b> ").append(enhancedNode.getDisplayName()).append("<br>");
                if (enhancedNode.getDataSources() != null) {
                    sb.append("    <b>Data Sources:</b> ").append(String.join(", ", enhancedNode.getDataSources())).append("<br>");
                }
                if (enhancedNode.getDescription() != null) {
                    sb.append("    <b>Description:</b> ").append(enhancedNode.getDescription()).append("<br>");
                }
                if (enhancedNode.getSynonyms() != null) {
                    sb.append("    <b>Synonyms:</b> ").append(String.join(", ", enhancedNode.getSynonyms())).append("<br>");
                }
            }
            sb.append("<br>");
        }
        return sb.toString();
    }

    private String stringifyEnhancedNodes(List<NetdrexKGNodeEnhanced> enhancedNodes) {
        StringBuilder sb = new StringBuilder();
        for (NetdrexKGNodeEnhanced node : enhancedNodes) {
            sb.append("Node Type: ").append(node.getNodeType()).append("\n");
            sb.append("Node Value: ").append(node.getNodeValue()).append("\n");
            sb.append("Sub Question: ").append(node.getSubQuestion()).append("\n");
            sb.append("Needs Filter: ").append(node.getNeedsFilter()).append("\n");
            if (node.getNeedsFilter() != null && !node.getNeedsFilter()) {
                sb.append("  - No filtering applied.\n\n");
                continue;
            }
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
