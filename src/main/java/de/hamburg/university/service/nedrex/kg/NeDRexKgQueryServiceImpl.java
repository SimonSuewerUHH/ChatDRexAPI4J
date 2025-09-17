package de.hamburg.university.service.nedrex.kg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.ChatdrexConfig;
import de.hamburg.university.agent.bot.kg.NeDRexKGGraph;
import de.hamburg.university.agent.bot.kg.NeDRexKGNode;
import de.hamburg.university.service.nedrex.NeDRexApiClient;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class NeDRexKgQueryServiceImpl {

    @Inject
    @RestClient
    NeDRexApiClient neDRexApiClient;

    @Inject
    ChatdrexConfig config;

    public List<NeDRexKGNodeEnhanced> enhanceGraph(NeDRexKGGraph graph) {
        return enhanceGraph(graph, config.tools().kgQuery().minNodeScore());

    }

    public List<NeDRexKGNodeEnhanced> enhanceGraph(NeDRexKGGraph graph, double minScore) {
        return graph.getNodes().stream()
                .map(node -> {
                    if (node.getNeedsFilter() != null && !node.getNeedsFilter()) {
                        return new NeDRexKGNodeEnhanced(node, new ArrayList<>());
                    }
                    List<NeDRexSearchEmbeddingsNodeDTO> nodes = query(node, minScore);
                    return new NeDRexKGNodeEnhanced(node, nodes);
                })
                .toList();
    }

    public List<NeDRexSearchEmbeddingsNodeDTO> query(NeDRexKGNode node, double minScore) {
        String collection = mapNodeType(node.getNodeType());
        if (collection == null) {
            return new ArrayList<>();
        }
        String query = node.getNodeValue() + " (" + node.getSubQuestion() + ")";
        NeDRexSearchEmbeddingRequestDTO requestDTO = new NeDRexSearchEmbeddingRequestDTO();
        requestDTO.setQuery(query);
        requestDTO.setTop(config.tools().kgQuery().queryTopNode());
        requestDTO.setCollection(collection);
        try {
            List<List<NeDRexSearchEmbeddingsNodeResponseDTO>> result = queryEmbeddings(requestDTO);
            return result.stream()
                    .flatMap(Collection::stream)
                    .filter(f -> f.getScore() >= minScore)
                    .map(f -> (NeDRexSearchEmbeddingsNodeDTO) f)
                    .toList();
        } catch (ClientWebApplicationException e) {
            Log.errorf("Failed to query embeddings for node %s (%s)", node.getNodeValue(), e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            Log.errorf(e, "Failed to query embeddings for node %s", node.getNodeValue());
            return new ArrayList<>();
        }
    }

    public String fireNeo4jQuery(String cypher) {
        String result = neDRexApiClient.runQuery(cypher);
        if (StringUtils.isNotEmpty(result) && result.length() > config.tools().kgQuery().maxResultLength()) {
            return result.substring(0, config.tools().kgQuery().maxResultLength()) + "... (truncated)";
        }
        return result;
    }

    private List<List<NeDRexSearchEmbeddingsNodeResponseDTO>> queryEmbeddings(NeDRexSearchEmbeddingRequestDTO dto) {
        String body;
        try (Response resp = neDRexApiClient.queryEmbeddings(dto)) {
            if (resp.getStatus() == 204) return List.of();
            body = resp.readEntity(String.class);
            if (body == null || body.isBlank()) return List.of();
            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(
                    body,
                    new TypeReference<List<List<NeDRexSearchEmbeddingsNodeResponseDTO>>>() {
                    }
            );
        } catch (ClientWebApplicationException e) {
            Log.errorf("Failed to query embeddings: %s (%s)", dto.getQuery(), e.getMessage());
            throw new ClientWebApplicationException("Failed to queryEmbeddings", e);
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

    public List<NeDRexKGNodeEnhanced> enhanceFallbackNodes(List<NeDRexKGNodeEnhanced> enhancedNodes) {
        return enhanceFallbackNodes(enhancedNodes, config.tools().kgQuery().minNodeScore());

    }

    public List<NeDRexKGNodeEnhanced> enhanceFallbackNodes(List<NeDRexKGNodeEnhanced> enhancedNodes, double minScore) {
        return enhancedNodes.stream()
                .map(node -> {
                    if (node.getNeedsFilter() != null && node.getNeedsFilter()) {
                        return node;
                    }
                    List<NeDRexSearchEmbeddingsNodeDTO> nodes = query(node, minScore);
                    return new NeDRexKGNodeEnhanced(node, nodes);
                })
                .toList();
    }
}
