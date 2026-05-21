package de.hamburg.university.tool.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricSummary {
    private int n;
    private double mean;
    private double standardDeviation;
    private double confidence95Low;
    private double confidence95High;

    public static MetricSummary of(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return new MetricSummary();
        }

        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = 0.0;
        if (values.size() > 1) {
            for (double value : values) {
                variance += Math.pow(value - mean, 2);
            }
            variance /= values.size() - 1;
        }

        double standardDeviation = Math.sqrt(variance);
        double confidenceWidth = values.size() > 1
                ? 1.96 * standardDeviation / Math.sqrt(values.size())
                : 0.0;
        return new MetricSummary(
                values.size(),
                mean,
                standardDeviation,
                mean - confidenceWidth,
                mean + confidenceWidth
        );
    }
}
