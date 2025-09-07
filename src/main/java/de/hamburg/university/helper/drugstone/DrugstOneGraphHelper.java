package de.hamburg.university.helper.drugstone;

import de.hamburg.university.service.netdrex.diamond.DiamondResultsDTO;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DrugstOneGraphHelper {

    public DrugstOneNetworkDTO diamondToNetwork(DiamondResultsDTO in) {
        DrugstOneNetworkDTO out = new DrugstOneNetworkDTO();
        out.setNetworkType("diamond_tool");

        Map<String, DrugstOneNodeDTO> nodes = new LinkedHashMap<>();
        List<DrugstOneEdgeDTO> edges = new ArrayList<>();

        if (in.getSeeds() != null) {
            for (String pid : in.getSeeds()) {
                addNodeIfAbsent(nodes, pid, pid, "seednode");
            }
        }
        if (in.getDiamondNodes() != null) {
            for (String pid : in.getDiamondNodes()) {
                addNodeIfAbsent(nodes, pid, pid, "diamondnode");
            }
        }
        if (in.getEdges() != null) {
            for (Object e : in.getEdges()) {
                // edge comes as a list/array of [from, to]
                if (e instanceof List<?> list && list.size() >= 2) {
                    String from = safeString(list.get(0));
                    String to = safeString(list.get(1));
                    if (StringUtils.isNotBlank(from) && StringUtils.isNotBlank(to)) {
                        edges.add(newEdge(from, to, "default"));
                    }
                }
            }
        }

        out.setNodes(new ArrayList<>(nodes.values()));
        out.setEdges(edges);
        return out;
    }


    private void addNodeIfAbsent(Map<String, DrugstOneNodeDTO> nodes,
                                 String id, String label, String type) {
        if (StringUtils.isBlank(id)) return;
        nodes.computeIfAbsent(id, k -> {
            DrugstOneNodeDTO n = new DrugstOneNodeDTO();
            n.setId(id);
            n.setLabel(StringUtils.defaultIfBlank(label, id));
            n.setType(StringUtils.defaultIfBlank(type, "default"));
            return n;
        });
    }

    private DrugstOneEdgeDTO newEdge(String from, String to, String group) {
        DrugstOneEdgeDTO e = new DrugstOneEdgeDTO();
        e.setFrom(from);
        e.setTo(to);
        e.setGroup(StringUtils.defaultIfBlank(group, "default"));
        return e;
    }

    private String safeString(Object o) {
        return (o == null) ? null : String.valueOf(o);
    }
}
