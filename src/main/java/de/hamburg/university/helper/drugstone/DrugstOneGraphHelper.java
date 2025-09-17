package de.hamburg.university.helper.drugstone;

import de.hamburg.university.service.nedrex.NeDRexAPIInfoDTO;
import de.hamburg.university.service.nedrex.NeDRexService;
import de.hamburg.university.service.nedrex.diamond.DiamondResultsDTO;
import de.hamburg.university.service.nedrex.trustrank.TrustRankResultDTO;
import de.hamburg.university.service.nedrex.trustrank.TrustRankToolEdge;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DrugstOneGraphHelper {

    @Inject
    NeDRexService neDRexService;

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

    public DrugstOneNetworkDTO trustrankToNetwork(TrustRankResultDTO in) {
        DrugstOneNetworkDTO out = new DrugstOneNetworkDTO();
        out.setNetworkType("trustrank_tool");

        Map<String, DrugstOneNodeDTO> nodes = new LinkedHashMap<>();
        List<DrugstOneEdgeDTO> edges = new ArrayList<>();

        // Seed proteins
        if (in.getSeedProteins() != null) {
            for (String pid : in.getSeedProteins()) {
                String label = fetchProteinName(pid);
                addNodeIfAbsent(nodes, pid, label, "seednode");
            }
        }

        // Drugs
        if (in.getDrugNames() != null) {
            for (String raw : in.getDrugNames()) {
                String label = fetchDrugName(raw);
                String id = stripPrefix(raw);
                addNodeIfAbsent(nodes, id, label, "founddrug");
            }
        }

        // Edges
        if (in.getEdges() != null) {
            for (TrustRankToolEdge e : in.getEdges()) {
                String from = stripPrefix(e.getFrom());
                String to = stripPrefix(e.getTo());
                if (StringUtils.isNotBlank(from) && StringUtils.isNotBlank(to)) {
                    edges.add(newEdge(from, to, "default"));
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

    private static String safeString(Object o) {
        return (o == null) ? null : String.valueOf(o);
    }

    private static String stripPrefix(String id) {
        if (StringUtils.isBlank(id)) return id;
        if (id.startsWith("drugbank.")) {
            return id.replace("drugbank.", "");
        } else if (id.startsWith("uniprot.")) {
            return id.replace("uniprot.", "");
        } else if (id.startsWith("entrez.")) {
            return id.replace("entrez.", "");
        }
        return id;
    }

    private String fetchDrugName(String drugbankId) {
        try {
            List<NeDRexAPIInfoDTO> list = neDRexService.fetchInfo(drugbankId);
            return list.stream()
                    .map(NeDRexAPIInfoDTO::getDisplayName)
                    .filter(StringUtils::isNotEmpty)
                    .findFirst()
                    .orElse(drugbankId);
        } catch (Exception e) {
            Log.warnf(e, "Error fetching drug name for %s", drugbankId);
        }
        return drugbankId;
    }

    private String fetchProteinName(String uniprotId) {
        if (uniprotId.startsWith("uniprot.") || uniprotId.startsWith("entrez.")) {
            Log.debugf("Stripping prefix from %s", uniprotId);
        } else {
            uniprotId = "uniprot." + uniprotId;
        }
        try {
            List<NeDRexAPIInfoDTO> list = neDRexService.fetchInfo(uniprotId);
            return list.stream()
                    .map(NeDRexAPIInfoDTO::getDisplayName)
                    .filter(StringUtils::isNotEmpty)
                    .findFirst()
                    .orElse(uniprotId);
        } catch (Exception e) {
            Log.warnf(e, "Error fetching protein name for %s", uniprotId);
        }
        return uniprotId;
    }
}
