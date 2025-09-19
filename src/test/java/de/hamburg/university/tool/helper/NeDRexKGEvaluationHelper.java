package de.hamburg.university.tool.helper;

import de.hamburg.university.tool.pojo.Score;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class NeDRexKGEvaluationHelper {

    public static Score score(List<Map<String, String>> gold, List<Map<String, String>> ai) {
        boolean[] usedAi = new boolean[ai.size()];
        int hits = 0;

        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < gold.size(); i++) order.add(i);
        order.sort(Comparator.comparingInt(i -> possibleMatches(ai, gold.get(i)).size()));

        for (int gi : order) {
            List<Integer> candidates = possibleMatches(ai, gold.get(gi));
            for (int aj : candidates) {
                if (!usedAi[aj]) {
                    usedAi[aj] = true;
                    hits++;
                    break;
                }
            }
        }

        return new Score(hits, gold, ai);
    }

    private static List<Integer> possibleMatches(List<Map<String, String>> ai, Map<String, String> goldRow) {
        List<Integer> idx = new ArrayList<>();
        for (int j = 0; j < ai.size(); j++) {
            if (isSuperset(ai.get(j), goldRow)) idx.add(j);
        }
        return idx;
    }

    private static boolean isSuperset(Map<String, String> ai, Map<String, String> gold) {
        for (String gv : gold.values()) {
            if (!ai.containsValue(gv)) return false;
        }
        return true;
    }

}
