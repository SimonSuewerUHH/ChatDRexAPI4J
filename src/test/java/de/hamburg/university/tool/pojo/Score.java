package de.hamburg.university.tool.pojo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Score {
    private int hits;
    private int lengthGold;
    private int lengthAI;
    private double precision;
    private double recall;
    private double f1;

    public Score(int hits, List<Map<String, String>> gold, List<Map<String, String>> ai) {
        this.hits = hits;
        this.lengthGold = gold.size();
        this.lengthAI = ai.size();
        this.precision = ai.isEmpty() ? 0.0 : (double) hits / lengthAI;
        this.recall = gold.isEmpty() ? 0.0 : (double) hits / lengthGold;

        if(this.precision + this.recall == 0) {
            this.f1 = 0;
        } else {
            this.f1 = 2 * this.precision * this.recall / (this.precision + this.recall);
        }

    }

    @Override
    public String toString() {
        return "Score{" +
                "hits=" + hits +
                ", precision=" + precision +
                ", recall=" + recall +
                ", f1=" + f1 +
                '}';
    }

    public static Score average(List<Score> scores) {
        if (scores.isEmpty()) {
            return new Score(0, List.of(), List.of());
        }

        int totalHits = 0;
        double sumPrecision = 0.0;
        double sumRecall = 0.0;
        double sumF1 = 0.0;

        for (Score s : scores) {
            totalHits += s.getHits();
            sumPrecision += s.getPrecision();
            sumRecall += s.getRecall();
            sumF1 += s.getF1();
        }

        int n = scores.size();

        Score avg = new Score(0, List.of(), List.of());
        avg.setHits(totalHits);
        avg.setPrecision(sumPrecision / n);
        avg.setRecall(sumRecall / n);
        avg.setF1(sumF1 / n);

        return avg;
    }
}
