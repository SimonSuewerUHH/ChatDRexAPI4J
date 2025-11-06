package de.hamburg.university.helper.drugstone.cypher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.ChatWebsocketSender;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import de.hamburg.university.helper.drugstone.DrugstOneManager;
import de.hamburg.university.helper.drugstone.dto.DrugstOneEdgeDTO;
import de.hamburg.university.helper.drugstone.dto.DrugstOneNetworkDTO;
import de.hamburg.university.helper.drugstone.dto.DrugstOneNodeDTO;
import de.hamburg.university.service.nedrex.NeDRexApiClient;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class CypherToDrugstOne {

    @Inject
    @RestClient
    NeDRexApiClient neDRexApiClient;

    @Inject
    ChatWebsocketSender chatWebsocketSender;

    @Inject
    DrugstOneManager drugstOneManager;

    public void toDrugstOne(List<String> ids, ChatRequestDTO content, MultiEmitter<? super ChatResponseDTO> emitter) {

        ToolDTO toolDTO = new ToolDTO(Tools.DRUGST_ONE.name());
        toolDTO.setInput("Building Graph with " + ids.size() + " Entrez IDs");
        chatWebsocketSender.sendTool(toolDTO, content, emitter);


        String cypher = buildGenericNeighborQuery(ids);
        String raw = fireNeo4jQuery(cypher);
        toolDTO.addContent(cypher);
        toolDTO.addContent(raw);
        chatWebsocketSender.sendTool(toolDTO, content, emitter);
        List<CypherEdge> edges = parseNeo4jRows(raw);

        Map<String, DrugstOneNodeDTO> nodeMap = new LinkedHashMap<>();
        for (CypherEdge edgeDto : edges) {

            nodeMap.computeIfAbsent(edgeDto.getN1Id(), id -> {
                DrugstOneNodeDTO node = new DrugstOneNodeDTO();
                node.setId(id);
                node.setLabel(edgeDto.getN1DisplayName());
                node.setGroup(edgeDto.getN1Type().toLowerCase());
                return node;
            });

            nodeMap.computeIfAbsent(edgeDto.getN2Id(), id -> {
                DrugstOneNodeDTO node = new DrugstOneNodeDTO();
                node.setId(id);
                node.setLabel(edgeDto.getN2DisplayName());
                node.setGroup(edgeDto.getN2Type().toLowerCase());
                return node;
            });
        }

        List<DrugstOneEdgeDTO> graphEdges = edges.stream().map(dto -> {
            DrugstOneEdgeDTO edge = new DrugstOneEdgeDTO();
            edge.setFrom(dto.getN1Id());
            edge.setTo(dto.getN2Id());
            edge.setGroup("default");
            return edge;
        }).toList();

        DrugstOneNetworkDTO result = new DrugstOneNetworkDTO();
        result.setNodes(new ArrayList<>(nodeMap.values()));
        result.setEdges(graphEdges);
        result.setNetworkType("cypher_qa_tool");
        drugstOneManager.patchNetwork(content, result);
        drugstOneManager.stopAndSend(toolDTO, content, emitter);
    }

    public String fireNeo4jQuery(String cypher) {
        return neDRexApiClient.runQuery(cypher);
    }

    private String buildGenericNeighborQuery(List<String> ids) {
        String inList = ids.stream()
                .filter(StringUtils::isNotBlank)
                .map(s -> "\"" + s.trim() + "\"")
                .collect(Collectors.joining(","));

        return "MATCH (n1)-[r]->(n2) " +
                "WHERE n1.primaryDomainId IN [" + inList + "] " +
                "AND n2.primaryDomainId IN [" + inList + "] " +
                "RETURN { " +
                "  n1PrimaryDomainId: n1.primaryDomainId, " +
                "  n1Type: n1.type, " +
                "  n1DisplayName: n1.displayName, " +
                "  relationType: r.type, " +
                "  n2PrimaryDomainId: n2.primaryDomainId, " +
                "  n2Type: n2.type, " +
                "  n2DisplayName: n2.displayName " +
                "} AS edge";
    }

    private List<CypherEdge> parseNeo4jRows(String rawJson) {
        if (StringUtils.isBlank(rawJson)) return new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(rawJson, new TypeReference<List<CypherEdgeResult>>() {
                    }).stream()
                    .map(CypherEdgeResult::getEdge)
                    .toList();
        } catch (Exception e) {
            throw new UncheckedIOException(new java.io.IOException("Failed to parse Neo4j JSON", e));
        }
    }
}
