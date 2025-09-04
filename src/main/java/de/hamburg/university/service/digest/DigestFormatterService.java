package de.hamburg.university.service.digest;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.DecimalFormat;
import java.util.*;

@ApplicationScoped
public class DigestFormatterService {

    private static final DecimalFormat P_FMT = new DecimalFormat("0.###");

    public String formatDigestOutput(DigestResultsDTO results) {
        return formatDigestOutput(results, 0.05, 10);
    }

    public String formatDigestOutput(DigestResultsDTO results, double cutOff, int topN) {
        if (results == null || results.getPValues() == null || results.getPValues().getValues() == null) {
            return "No results: missing p-values.";
        }
        // In your Python, p-values were at results['p_values']['values']["JI-based"][DB]
        Map<String, Map<String, Double>> pValuesByMetric = results.getPValues().getValues();
        Map<String, Double> empiricalP = pValuesByMetric.getOrDefault("JI-based", Collections.emptyMap());

        if (empiricalP.isEmpty()) {
            return "No results: 'JI-based' empirical p-values not found.";
        }

        // Filter DBs by cutoff
        List<String> significantDbs = empiricalP.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue() < cutOff)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();

        if (significantDbs.isEmpty()) {
            return "No significant results found for cut-off empirical p-value " + cutOff + ".";
        }

        // mapped_ids shape: DB -> geneId -> [termIds...]
        Map<String, Map<String, List<String>>> mappedIds =
                results.getInputValues() != null ? results.getInputValues().getMappedIds() : null;

        StringBuilder out = new StringBuilder();
        out.append("The provided gene set has annotations in ")
                .append(significantDbs.size())
                .append(" databases. The enriched terms are:\n\n");

        for (String db : significantDbs) {
            double p = Optional.ofNullable(empiricalP.get(db)).orElse(Double.NaN);

            // Collect all terms across genes for this DB and count frequency
            Map<String, Integer> termCounts = new LinkedHashMap<>();
            List<String> genesWithAnnotations = new ArrayList<>();

            if (mappedIds != null) {
                Map<String, List<String>> geneToTerms = mappedIds.getOrDefault(db, Collections.emptyMap());

                for (Map.Entry<String, List<String>> g : geneToTerms.entrySet()) {
                    String gene = g.getKey();
                    List<String> terms = Optional.ofNullable(g.getValue()).orElse(Collections.emptyList());
                    if (!terms.isEmpty()) {
                        genesWithAnnotations.add(gene);
                    }
                    for (String t : terms) {
                        termCounts.merge(t, 1, Integer::sum);
                    }
                }
            }

            // Sort terms by count desc, then lexicographically to be stable
            List<Map.Entry<String, Integer>> sortedTerms = termCounts.entrySet().stream()
                    .sorted((a, b) -> {
                        int cmp = Integer.compare(b.getValue(), a.getValue());
                        return (cmp != 0) ? cmp : a.getKey().compareTo(b.getKey());
                    })
                    .limit(Math.max(topN, 0))
                    .toList();

            out.append("- ").append(db)
                    .append(" (statistical significance: ").append(P_FMT.format(p)).append("):");

            if (sortedTerms.isEmpty()) {
                out.append(" no terms found\n");
                continue;
            }

            // Same inline list style as your Python "create_structure"
            out.append("\n");
            for (Map.Entry<String, Integer> e : sortedTerms) {
                // We don’t pull external descriptions here for robustness; just list terms (+count)
                out.append("  ").append(e.getKey());
                if (e.getValue() != null && e.getValue() > 1) {
                    out.append(" (×").append(e.getValue()).append(")");
                }
                out.append("\n");
            }
        }

        return out.toString().trim();
    }

}
