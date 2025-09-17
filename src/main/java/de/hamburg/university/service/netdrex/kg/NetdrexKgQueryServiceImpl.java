package de.hamburg.university.service.netdrex.kg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.ChatdrexConfig;
import de.hamburg.university.agent.bot.kg.NetdrexKGGraph;
import de.hamburg.university.agent.bot.kg.NetdrexKGNode;
import de.hamburg.university.service.netdrex.NetdrexApiClient;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class NetdrexKgQueryServiceImpl {

    @Inject
    @RestClient
    NetdrexApiClient netdrexApiClient;

    @Inject
    ChatdrexConfig config;

    public List<NetdrexKGNodeEnhanced> enhanceGraph(NetdrexKGGraph graph) {
        return enhanceGraph(graph, config.tools().kgQuery().minNodeScore());

    }

    public List<NetdrexKGNodeEnhanced> enhanceGraph(NetdrexKGGraph graph, double minScore) {
        return graph.getNodes().stream()
                .map(node -> {
                    if (node.getNeedsFilter() != null && !node.getNeedsFilter()) {
                        return new NetdrexKGNodeEnhanced(node, new ArrayList<>());
                    }
                    List<NetdrexSearchEmbeddingsNodeDTO> nodes = query(node, minScore);
                    return new NetdrexKGNodeEnhanced(node, nodes);
                })
                .toList();
    }

    public List<NetdrexSearchEmbeddingsNodeDTO> query(NetdrexKGNode node, double minScore) {
        String collection = mapNodeType(node.getNodeType());
        if (collection == null) {
            return new ArrayList<>();
        }
        String query = node.getNodeValue() + " (" + node.getSubQuestion() + ")";
        NetdrexSearchEmbeddingRequestDTO requestDTO = new NetdrexSearchEmbeddingRequestDTO();
        requestDTO.setQuery(query);
        requestDTO.setTop(config.tools().kgQuery().queryTopNode());
        requestDTO.setCollection(collection);
        try {
            List<List<NetdrexSearchEmbeddingsNodeResponseDTO>> result = queryEmbeddings(requestDTO);
            return result.stream()
                    .flatMap(Collection::stream)
                    .filter(f -> f.getScore() >= minScore)
                    .map(f -> (NetdrexSearchEmbeddingsNodeDTO) f)
                    .toList();
        } catch (Exception e) {
            Log.errorf(e, "Failed to query embeddings for node %s", node.getNodeValue());
            return new ArrayList<>();
        }
    }

    public String fireNeo4jQuery(String cypher) {
        String result = netdrexApiClient.runQuery(cypher);
        if (StringUtils.isNotEmpty(result) && result.length() > config.tools().kgQuery().maxResultLength()) {
            return result.substring(0, config.tools().kgQuery().maxResultLength()) + "... (truncated)";
        }
        return result;
    }

    private List<List<NetdrexSearchEmbeddingsNodeResponseDTO>> queryEmbeddings(NetdrexSearchEmbeddingRequestDTO dto) {
        String body;
        try (Response resp = netdrexApiClient.queryEmbeddings(dto)) {
            if (resp.getStatus() == 204) return List.of();
            body = resp.readEntity(String.class);
            if (body == null || body.isBlank()) return List.of();
            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(
                    body,
                    new TypeReference<List<List<NetdrexSearchEmbeddingsNodeResponseDTO>>>() {
                    }
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse embeddings response", e);
        }
    }

    private String mapNodeType(String nodeType) {
        if (nodeType == null) {
            return null;
        }

        return switch (nodeType) {
            case "disorder" -> "Disorder";
            case "drug" -> "Drug";
            case "gene" -> "Gene";
            case "genomic_variant" -> "VariantAssociatedWithDisorder";
            case "go" -> "GO";
            case "pathway" -> "Pathway";
            case "phenotype" -> "Phenotype";
            case "protein" -> "Protein";
            case "side_effect" -> "SideEffect";
            case "signature" -> "Signature";
            case "tissue" -> "Tissue";
            default -> null; // oder throw new IllegalArgumentException
        };
    }

    public List<NetdrexKGNodeEnhanced> enhanceFallbackNodes(List<NetdrexKGNodeEnhanced> enhancedNodes) {
        return enhanceFallbackNodes(enhancedNodes, config.tools().kgQuery().minNodeScore());

    }

    public List<NetdrexKGNodeEnhanced> enhanceFallbackNodes(List<NetdrexKGNodeEnhanced> enhancedNodes, double minScore) {
        return enhancedNodes.stream()
                .map(node -> {
                    if (node.getNeedsFilter() != null && node.getNeedsFilter()) {
                        return node;
                    }
                    List<NetdrexSearchEmbeddingsNodeDTO> nodes = query(node, minScore);
                    return new NetdrexKGNodeEnhanced(node, nodes);
                })
                .toList();
    }
}
