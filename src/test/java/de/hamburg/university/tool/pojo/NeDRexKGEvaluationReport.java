package de.hamburg.university.tool.pojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.logging.Log;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NeDRexKGEvaluationReport {
    private String model;
    private int configuredRuns;
    private int totalEvaluations;
    private int totalPrimaryCypherAnswers;
    private int totalFallbackAnswers;
    private int totalCorrectAnswers;
    private int totalPrimaryCorrectAnswers;
    private int totalFallbackCorrectAnswers;
    private MetricSummary cypherPrecision;
    private MetricSummary cypherRecall;
    private MetricSummary cypherF1;
    private MetricSummary answerAccuracy;
    private MetricSummary primaryCypherAnswerRate;
    private MetricSummary fallbackRate;
    private MetricSummary primaryCypherAnswerAccuracy;
    private MetricSummary fallbackAnswerAccuracy;
    private List<RunMetrics> runs;

    public static NeDRexKGEvaluationReport fromCypherScores(String model, int configuredRuns, List<QuestionScore> scores) {
        NeDRexKGEvaluationReport report = base(model, configuredRuns);
        List<RunMetrics> runs = groupQuestionScores(scores).entrySet().stream()
                .map(entry -> RunMetrics.fromQuestionScores(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(RunMetrics::getRun))
                .toList();

        report.setRuns(runs);
        report.setTotalEvaluations(scores.size());
        report.setCypherPrecision(MetricSummary.of(runs.stream().map(RunMetrics::getCypherPrecision).toList()));
        report.setCypherRecall(MetricSummary.of(runs.stream().map(RunMetrics::getCypherRecall).toList()));
        report.setCypherF1(MetricSummary.of(runs.stream().map(RunMetrics::getCypherF1).toList()));
        return report;
    }

    public static NeDRexKGEvaluationReport fromAnswerScores(String model, int configuredRuns, List<AiAnswerCypher> answers) {
        NeDRexKGEvaluationReport report = base(model, configuredRuns);
        List<RunMetrics> runs = groupAnswerScores(answers).entrySet().stream()
                .map(entry -> RunMetrics.fromAnswers(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(RunMetrics::getRun))
                .toList();

        report.setRuns(runs);
        report.setTotalEvaluations(answers.size());
        report.setTotalCorrectAnswers((int) answers.stream().filter(AiAnswerCypher::getIsCorrectOrFalse).count());
        report.setTotalFallbackAnswers((int) answers.stream().filter(AiAnswerCypher::isFallback).count());
        report.setTotalPrimaryCypherAnswers(answers.size() - report.getTotalFallbackAnswers());
        report.setTotalFallbackCorrectAnswers((int) answers.stream()
                .filter(AiAnswerCypher::isFallback)
                .filter(AiAnswerCypher::getIsCorrectOrFalse)
                .count());
        report.setTotalPrimaryCorrectAnswers(report.getTotalCorrectAnswers() - report.getTotalFallbackCorrectAnswers());
        report.setAnswerAccuracy(MetricSummary.of(runs.stream().map(RunMetrics::getAnswerAccuracy).toList()));
        report.setPrimaryCypherAnswerRate(MetricSummary.of(runs.stream().map(RunMetrics::getPrimaryCypherAnswerRate).toList()));
        report.setFallbackRate(MetricSummary.of(runs.stream().map(RunMetrics::getFallbackRate).toList()));
        report.setPrimaryCypherAnswerAccuracy(MetricSummary.of(runs.stream().map(RunMetrics::getPrimaryCypherAnswerAccuracy).toList()));
        report.setFallbackAnswerAccuracy(MetricSummary.of(runs.stream().map(RunMetrics::getFallbackAnswerAccuracy).toList()));
        return report;
    }

    public static void printJsonFile(NeDRexKGEvaluationReport report, Path file) {
        try {
            Files.createDirectories(file.getParent());
            ObjectMapper mapper = new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(file.toFile(), report);
            Log.infof("Evaluation report written: %s", file.toAbsolutePath());
        } catch (Exception e) {
            Log.error("Failed to write evaluation report", e);
        }
    }

    public static void printMarkdownFile(NeDRexKGEvaluationReport report, Path file) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, report.toMarkdown());
            Log.infof("Evaluation report written: %s", file.toAbsolutePath());
        } catch (Exception e) {
            Log.error("Failed to write evaluation report", e);
        }
    }

    private String toMarkdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("# NeDRex KG Evaluation Report\n\n");
        sb.append("- Model: ").append(model).append("\n");
        sb.append("- Configured runs: ").append(configuredRuns).append("\n");
        sb.append("- Total evaluations: ").append(totalEvaluations).append("\n");
        if (totalPrimaryCypherAnswers + totalFallbackAnswers > 0) {
            sb.append("- Primary Cypher answers: ").append(totalPrimaryCypherAnswers).append("\n");
            sb.append("- GraphRAG fallback answers: ").append(totalFallbackAnswers).append("\n");
            sb.append("- Correct answers: ").append(totalCorrectAnswers).append("\n");
        }
        sb.append("\n");

        appendMetric(sb, "Cypher precision", cypherPrecision);
        appendMetric(sb, "Cypher recall", cypherRecall);
        appendMetric(sb, "Cypher F1", cypherF1);
        appendMetric(sb, "Answer accuracy", answerAccuracy);
        appendMetric(sb, "Primary Cypher answer rate", primaryCypherAnswerRate);
        appendMetric(sb, "GraphRAG fallback rate", fallbackRate);
        appendMetric(sb, "Primary Cypher answer accuracy", primaryCypherAnswerAccuracy);
        appendMetric(sb, "GraphRAG fallback answer accuracy", fallbackAnswerAccuracy);

        sb.append("\n## Per Run\n\n");
        sb.append("| Run | Total | Answer accuracy | Primary rate | Fallback rate | Cypher precision | Cypher recall | Cypher F1 |\n");
        sb.append("| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |\n");
        for (RunMetrics run : runs) {
            sb.append("| ").append(run.getRun())
                    .append(" | ").append(run.getTotal())
                    .append(" | ").append(format(run.getAnswerAccuracy()))
                    .append(" | ").append(format(run.getPrimaryCypherAnswerRate()))
                    .append(" | ").append(format(run.getFallbackRate()))
                    .append(" | ").append(format(run.getCypherPrecision()))
                    .append(" | ").append(format(run.getCypherRecall()))
                    .append(" | ").append(format(run.getCypherF1()))
                    .append(" |\n");
        }
        return sb.toString();
    }

    private static NeDRexKGEvaluationReport base(String model, int configuredRuns) {
        NeDRexKGEvaluationReport report = new NeDRexKGEvaluationReport();
        report.setModel(model);
        report.setConfiguredRuns(configuredRuns);
        report.setRuns(new ArrayList<>());
        return report;
    }

    private static Map<Integer, List<QuestionScore>> groupQuestionScores(List<QuestionScore> scores) {
        return scores.stream().collect(Collectors.groupingBy(QuestionScore::getRun, TreeMap::new, Collectors.toList()));
    }

    private static Map<Integer, List<AiAnswerCypher>> groupAnswerScores(List<AiAnswerCypher> answers) {
        return answers.stream().collect(Collectors.groupingBy(AiAnswerCypher::getRun, TreeMap::new, Collectors.toList()));
    }

    private static void appendMetric(StringBuilder sb, String name, MetricSummary metric) {
        if (metric == null || metric.getN() == 0) {
            return;
        }
        sb.append("- ").append(name)
                .append(": mean=").append(format(metric.getMean()))
                .append(", sd=").append(format(metric.getStandardDeviation()))
                .append(", 95% CI=[").append(format(metric.getConfidence95Low()))
                .append(", ").append(format(metric.getConfidence95High()))
                .append("], n=").append(metric.getN())
                .append("\n");
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.4f", value);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RunMetrics {
        private int run;
        private int total;
        private int correctAnswers;
        private int primaryCypherAnswers;
        private int fallbackAnswers;
        private int primaryCorrectAnswers;
        private int fallbackCorrectAnswers;
        private double cypherPrecision;
        private double cypherRecall;
        private double cypherF1;
        private double answerAccuracy;
        private double primaryCypherAnswerRate;
        private double fallbackRate;
        private double primaryCypherAnswerAccuracy;
        private double fallbackAnswerAccuracy;

        private static RunMetrics fromQuestionScores(int run, List<QuestionScore> scores) {
            RunMetrics metrics = new RunMetrics();
            metrics.setRun(run);
            metrics.setTotal(scores.size());
            Score average = Score.average(scores.stream().map(QuestionScore::getScore).toList());
            metrics.setCypherPrecision(average.getPrecision());
            metrics.setCypherRecall(average.getRecall());
            metrics.setCypherF1(average.getF1());
            return metrics;
        }

        private static RunMetrics fromAnswers(int run, List<AiAnswerCypher> answers) {
            RunMetrics metrics = new RunMetrics();
            metrics.setRun(run);
            metrics.setTotal(answers.size());
            metrics.setCorrectAnswers((int) answers.stream().filter(AiAnswerCypher::getIsCorrectOrFalse).count());
            metrics.setFallbackAnswers((int) answers.stream().filter(AiAnswerCypher::isFallback).count());
            metrics.setPrimaryCypherAnswers(answers.size() - metrics.getFallbackAnswers());
            metrics.setFallbackCorrectAnswers((int) answers.stream()
                    .filter(AiAnswerCypher::isFallback)
                    .filter(AiAnswerCypher::getIsCorrectOrFalse)
                    .count());
            metrics.setPrimaryCorrectAnswers(metrics.getCorrectAnswers() - metrics.getFallbackCorrectAnswers());
            metrics.setAnswerAccuracy(ratio(metrics.getCorrectAnswers(), metrics.getTotal()));
            metrics.setPrimaryCypherAnswerRate(ratio(metrics.getPrimaryCypherAnswers(), metrics.getTotal()));
            metrics.setFallbackRate(ratio(metrics.getFallbackAnswers(), metrics.getTotal()));
            metrics.setPrimaryCypherAnswerAccuracy(ratio(metrics.getPrimaryCorrectAnswers(), metrics.getPrimaryCypherAnswers()));
            metrics.setFallbackAnswerAccuracy(ratio(metrics.getFallbackCorrectAnswers(), metrics.getFallbackAnswers()));
            return metrics;
        }

        private static double ratio(int numerator, int denominator) {
            return denominator == 0 ? 0.0 : numerator / (double) denominator;
        }
    }
}
