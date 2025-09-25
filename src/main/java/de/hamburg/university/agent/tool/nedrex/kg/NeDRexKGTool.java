package de.hamburg.university.agent.tool.nedrex.kg;

import de.hamburg.university.ChatdrexConfig;
import de.hamburg.university.agent.bot.kg.NeDRexKGBot;
import de.hamburg.university.agent.bot.kg.NeDRexKGGraph;
import de.hamburg.university.agent.bot.kg.NeDRexKGNode;
import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import de.hamburg.university.helper.drugstone.cypher.CypherResultIdExtractor;
import de.hamburg.university.helper.drugstone.cypher.CypherToDrugstOne;
import de.hamburg.university.service.nedrex.kg.NeDRexKGNodeEnhanced;
import de.hamburg.university.service.nedrex.kg.NeDRexKgQueryServiceImpl;
import de.hamburg.university.service.nedrex.kg.NeDRexSearchEmbeddingsNodeDTO;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class NeDRexKGTool {

    @Inject
    ChatWebsocketSender chatWebsocketSender;

    @Inject
    NeDRexKGBot nedrexKGBot;

    @Inject
    NeDRexKgQueryServiceImpl nedrexKgQueryService;

    @Inject
    ChatdrexConfig config;

    @Inject
    CypherToDrugstOne cypherToDrugstOne;

    public NeDRexKGGraph decomposeToNodes(String question) {
        return decomposeToNodes(question, "");
    }

    public NeDRexKGGraph decomposeToNodes(String question, String context) {
        return nedrexKGBot.decomposeToNodes(question, context);
    }

    public String generateCypher(String question) {
        NeDRexKGGraph questionGraph = decomposeToNodes(question);
        List<NeDRexKGNodeEnhanced> enhancedNodes = nedrexKgQueryService.enhanceGraph(questionGraph);
        String enhancedNodesString = stringifyEnhancedNodes(enhancedNodes);
        double minScore = config.tools().kgQuery().minGeneDisorderScore();
        String query = nedrexKGBot.generateCypherQuery(question, enhancedNodesString, "", minScore);
        return query;
    }

    public String answer(String question, String context, ChatRequestDTO content, MultiEmitter<? super ChatResponseDTO> emitter) {
        ToolDTO toolDTO = new ToolDTO(Tools.NEDREX_KG.name());
        toolDTO.setInput(question);
        double minScore = config.tools().kgQuery().minGeneDisorderScore();

        try {
            chatWebsocketSender.sendTool(toolDTO, content, emitter);

            NeDRexKGGraph questionGraph = decomposeToNodes(question, context);
            toolDTO.addContent("<h3>Decomposed Nodes:</h3>");
            toolDTO.addContent(stringifyNodesToHtml(questionGraph.getNodes()));
            chatWebsocketSender.sendTool(toolDTO, content, emitter);
            List<NeDRexKGNodeEnhanced> enhancedNodes = nedrexKgQueryService.enhanceGraph(questionGraph);
            List<String> ids = getIds(enhancedNodes);
            String enhancedNodesString = stringifyEnhancedNodesToHTML(enhancedNodes);
            toolDTO.addContent("<h3>Enhanced Nodes:</h3>");
            toolDTO.addContent(enhancedNodesString);
            chatWebsocketSender.sendTool(toolDTO, content, emitter);

            String oldQuery = "";
            String newQuery = "";
            final int maxAttempts = config.tools().kgQuery().retries();
            for (int i = 0; i < maxAttempts; i++) {
                try {
                    newQuery = nedrexKGBot.generateCypherQuery(question, enhancedNodesString, oldQuery, minScore);
                    oldQuery += "\n " + i + ". " + newQuery;
                    toolDTO.addContent("<h3>Neo4j Query:</h3>");
                    toolDTO.addContent(newQuery);
                    chatWebsocketSender.sendTool(toolDTO, content, emitter);

                    String result = nedrexKgQueryService.fireNeo4jQuery(newQuery);
                    toolDTO.addContent("<h3>Neo4j Result:</h3>");
                    toolDTO.addContent(result);
                    chatWebsocketSender.sendTool(toolDTO, content, emitter);

                    Log.infof("Generated Cypher Query: \n%s\nfor question %s", newQuery, question);
                    if (StringUtils.isEmpty(result)) {
                        Log.infof("Empty result for query: %s", newQuery);
                        continue;
                    }
                    if (i == maxAttempts - 1) {
                        Log.infof("Final attempt %d, returning result even if it might be incomplete.", i + 1);
                        break;
                    }
                    List<String> cypherIds = CypherResultIdExtractor.extractResults(result);
                    cypherIds.addAll(ids);
                    cypherToDrugstOne.toDrugstOne(cypherIds, content, emitter);
                    String answer = nedrexKGBot.answerQuestion(question, result);
                    toolDTO.addContent(answer);
                    toolDTO.setStop();
                    chatWebsocketSender.sendTool(toolDTO, content, emitter);

                    return answer;
                } catch (ClientWebApplicationException e) {
                    Log.errorf("Failed to query: %s (%s)", newQuery, e.getMessage());
                } catch (Exception e) {
                    Log.warnf(e, "Attempt %d: Failed to generate answer for question: %s", i + 1, question);
                }

            }
            List<NeDRexKGNodeEnhanced> enhancedNodesFallback = nedrexKgQueryService.enhanceFallbackNodes(enhancedNodes);
            String enhancedNodesFallbackString = stringifyEnhancedNodes(enhancedNodesFallback);
            toolDTO.addContent("<h3>Query Failed => Fallback</h3>");
            toolDTO.addContent("<h3>Enhanced Nodes Fallback:</h3>");
            toolDTO.addContent(enhancedNodesString);
            chatWebsocketSender.sendTool(toolDTO, content, emitter);

            String answer = nedrexKGBot.answerFallbackQuestion(question, enhancedNodesFallbackString);
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

    @Tool("Answer a question about biomedical nodes such as drugs, proteins, disorders, or genes using the NeDRexDB or NeDRex knowledge graph.")
    public String answer(@P("The question") String question) {
        return answer(question, "", null, null);
    }


    private String stringifyNodesToHtml(List<NeDRexKGNode> enhancedNodes) {
        StringBuilder sb = new StringBuilder();
        for (NeDRexKGNode node : enhancedNodes) {
            sb.append("<b>Node Type:</b> ").append(node.getNodeType()).append("<br>");
            sb.append("<b>Node Value:</b> ").append(node.getNodeValue()).append("<br>");
            sb.append("<b>Sub Question:</b> ").append(node.getSubQuestion()).append("<br>");
            sb.append("<b>Needs Filter:</b> ").append(node.getNeedsFilter()).append("<br>");
            sb.append("<br>");
        }
        return sb.toString();
    }

    public String stringifyEnhancedNodesToHTML(List<NeDRexKGNodeEnhanced> enhancedNodes) {
        StringBuilder sb = new StringBuilder();
        for (NeDRexKGNodeEnhanced node : enhancedNodes) {
            sb.append("<b>Node Type:</b> ").append(node.getNodeType()).append("<br>");
            sb.append("<b>Node Value:</b> ").append(node.getNodeValue()).append("<br>");
            sb.append("<b>Sub Question:</b> ").append(node.getSubQuestion()).append("<br>");
            sb.append("<b>Needs Filter:</b> ").append(node.getNeedsFilter()).append("<br>");
            if (node.getNeedsFilter() != null && !node.getNeedsFilter()) {
                sb.append("  - No filtering applied.<br><br>");
                continue;
            }
            sb.append("<b>Enhanced Nodes:</b><br>");
            for (NeDRexSearchEmbeddingsNodeDTO enhancedNode : node.getNodes()) {
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

    public List<String> getIds(List<NeDRexKGNodeEnhanced> enhancedNodes) {
        List<String> ids = new ArrayList<>();
        for (NeDRexKGNodeEnhanced node : enhancedNodes) {
            for (NeDRexSearchEmbeddingsNodeDTO enhancedNode : node.getNodes()) {
                if (enhancedNode.getPrimaryDomainId() != null) {
                    ids.add(enhancedNode.getPrimaryDomainId());
                }
            }
        }
        return ids;
    }

    public String stringifyEnhancedNodes(List<NeDRexKGNodeEnhanced> enhancedNodes) {
        StringBuilder sb = new StringBuilder();
        for (NeDRexKGNodeEnhanced node : enhancedNodes) {
            sb.append("Node Type: ").append(node.getNodeType()).append("\n");
            sb.append("Node Value: ").append(node.getNodeValue()).append("\n");
            sb.append("Sub Question: ").append(node.getSubQuestion()).append("\n");
            sb.append("Needs Filter: ").append(node.getNeedsFilter()).append("\n");
            if (node.getNeedsFilter() != null && !node.getNeedsFilter()) {
                sb.append("  - No filtering applied.\n\n");
                continue;
            }
            sb.append("Enhanced Nodes:\n");
            for (NeDRexSearchEmbeddingsNodeDTO enhancedNode : node.getNodes()) {
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
