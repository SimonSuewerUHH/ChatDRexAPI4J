package de.hamburg.university.service.digest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.service.mygene.MyGeneClient;
import de.hamburg.university.service.mygene.MyGeneGoResponseDTO;
import de.hamburg.university.service.mygene.MyGeneGoTermDTO;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class DigestFormatterService {

    private static final Logger LOG = Logger.getLogger(DigestFormatterService.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @RestClient
    MyGeneClient myGeneClient;

    public DigestToolResultDTO formatDigestOutputStructured(DigestResultsDTO results) {
        return formatDigestOutputStructured(results, 0.6, 5);
    }

    /**
     * Full Python-equivalent: returns a structured table ("digest_out") like the pandas result.
     */
    public DigestToolResultDTO formatDigestOutputStructured(DigestResultsDTO results, double cutOff, int topN) {
        if (results == null
                || results.getPValues() == null
                || results.getPValues().getValues() == null) {
            throw new IllegalArgumentException("Missing p_values in results");
        }

        // Python: p_series = DataFrame.from_dict(results['p_values']['values'])
        Map<String, Map<String, Double>> pValuesByMetric = results.getPValues().getValues();
        Map<String, Double> empiricalP = pValuesByMetric.getOrDefault("JI-based", Collections.emptyMap());

        if (empiricalP.isEmpty()) {
            throw new IllegalArgumentException("No 'JI-based' empirical p-values found");
        }

        // Python: r = concat(...).rename(JI-based -> empirical_p_value)
        // Python: mask = r["empirical_p_value"] < cut_off
        // Keep only DBs with p < cutOff
        List<String> dbsPassing = empiricalP.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue() < cutOff)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (dbsPassing.isEmpty()) {
            throw new IllegalArgumentException("No significant results found for cut-off empirical p-value " + cutOff);
        }

        // Python: df_exploded from mapped_ids -> rows(database,gene,db_terms)
        Map<String, Map<String, List<String>>> mappedIds =
                results.getInputValues() != null ? results.getInputValues().getMappedIds() : null;

        List<DigestToolResultDTO.Row> rows = new ArrayList<>();

        for (String db : dbsPassing) {
            Double p = empiricalP.get(db);

            // Explode mapped_ids for this DB
            Map<String, List<String>> geneToTerms = mappedIds != null
                    ? mappedIds.getOrDefault(db, Collections.emptyMap())
                    : Collections.emptyMap();

            // Aggregate like aggregate_df:
            // - genes: aggregated list (unique, insertion order)
            // - db_terms: aggregated term counts (BUT preserve insertion order like Python path before select_top_n)
            LinkedHashSet<String> genesOrdered = new LinkedHashSet<>();
            LinkedHashMap<String, Integer> termCounts = new LinkedHashMap<>();

            geneToTerms.forEach((gene, terms) -> {
                if (terms != null && !terms.isEmpty()) {
                    genesOrdered.add(gene);
                    for (String t : terms) {
                        // If "aggregate_db_terms" sorted differently in Python, you can swap to a LinkedHashMap
                        // fed by a pre-sorted list. Here we preserve natural encounter order, then count.
                        termCounts.merge(t, 1, Integer::sum);
                    }
                }
            });

            // Python: digest_out['db_terms'] = digest_out['db_terms'].apply(lambda x: select_top_n(x, N))
            // select_top_n: "terms.items()[:n]" => take FIRST N pairs IN CURRENT ORDER (no resort)
            LinkedHashMap<String, Integer> topTerms = selectTopN(termCounts, topN);

            // Python: mg_query(row) -> descriptions aligned with db_terms order
            List<String> descriptions = resolveDescriptions(db, new ArrayList<>(topTerms.keySet()));

            DigestToolResultDTO.Row row = new DigestToolResultDTO.Row(
                    db,
                    new ArrayList<>(genesOrdered),
                    topTerms,
                    p,
                    descriptions
            );
            rows.add(row);
        }

        // Python often keeps original/groupby order; here we keep discover order of dbsPassing.
        return new DigestToolResultDTO(rows);
    }

    /**
     * Convenience: return JSON string identical to Python's `json.dumps(..., indent=4)`.
     */
    public String formatDigestOutputJson(DigestResultsDTO results, double cutOff, int topN) {
        DigestToolResultDTO dto = formatDigestOutputStructured(results, cutOff, topN);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize digest_out", e);
        }
    }

    // ==== helpers =====================================================================================

    private LinkedHashMap<String, Integer> selectTopN(Map<String, Integer> terms, int n) {
        LinkedHashMap<String, Integer> out = new LinkedHashMap<>();
        if (n <= 0 || terms == null || terms.isEmpty()) return out;

        int i = 0;
        for (Map.Entry<String, Integer> e : terms.entrySet()) {
            out.put(e.getKey(), e.getValue());
            if (++i >= n) break;
        }
        return out;
    }

    private List<String> resolveDescriptions(String db, List<String> termIds) {
        if (termIds == null || termIds.isEmpty()) return Collections.emptyList();

        try {
            return switch (db) {
                case "GO.BP" -> queryGoTerms(termIds, "go.BP");
                case "GO.MF" -> queryGoTerms(termIds, "go.MF");
                case "GO.CC" -> queryGoTerms(termIds, "go.CC");
                default -> queryGoTerms(termIds, "go.CC");
            };
            /*return keggService.map(svc -> svc.describeKegg(termIds))
                            .orElse(Collections.nCopies(termIds.size(), null));*/
        } catch (Exception e) {
            LOG.warnf(e, "Description lookup failed for db=%s", db);
            // Match Python behavior: continue even if lookup fails
            return Collections.nCopies(termIds.size(), null);
        }
    }

    private List<String> queryGoTerms(List<String> goIds, String aspect) {
        if (goIds == null || goIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return goIds.stream()
                    .map(id -> id.replaceAll("GO:", ""))
                    .map(id -> myGeneClient.query(id, aspect, "go", 1))
                    .flatMap(res -> res.getHits().stream())
                    .map(MyGeneGoResponseDTO::getGo)
                    .filter(Objects::nonNull)
                    .flatMap(g -> g.getCategories().values().stream())
                    .flatMap(List::stream)
                    .map(MyGeneGoTermDTO::getTerm)
                    .toList();
        } catch (Exception e) {
            Log.errorf(e, "Failed to query go terms for Id: %s and aspect: %s", StringUtils.join(", ", goIds), aspect);
            return Collections.emptyList();
        }
    }
}