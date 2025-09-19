package de.hamburg.university.tool.pojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.logging.Log;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionScore {
    private String category;
    private String question;
    private String goldenCypher;
    private String aiCypher;
    private Score score;

    public static void printCsvFile(List<QuestionScore> rows, Path file) {
        try {
            Files.createDirectories(file.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                w.write("category,question,hits,lengthGold,lengthAI,precision,recall,f1\n");
                for (QuestionScore row : rows) {
                    Score s = row.getScore();
                    int hits = s.getHits(); // adapt if Score uses different accessor
                    w.write(csv(row.getCategory()));
                    w.write(",");
                    w.write(csv(row.getQuestion()));
                    w.write(",");
                    w.write(Integer.toString(hits));
                    w.write(",");
                    w.write(Integer.toString(s.getLengthGold()));
                    w.write(",");
                    w.write(Integer.toString(s.getLengthAI()));
                    w.write(",");
                    w.write(Double.toString(s.getPrecision()));
                    w.write(",");
                    w.write(Double.toString(s.getRecall()));
                    w.write(",");
                    w.write(Double.toString(s.getF1()));
                    w.newLine();
                }
            }
            Log.infof("CSV written: %s", file.toAbsolutePath());
        } catch (Exception e) {
            Log.error("Failed to write CSV", e);
        }
    }

    public static void printJsonFile(List<QuestionScore> rows, Path file) {
        try {
            Files.createDirectories(file.getParent());
            ObjectMapper mapper = new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT);

            mapper.writeValue(file.toFile(), rows);

            Log.infof("JSON written: %s", file.toAbsolutePath());
        } catch (Exception e) {
            Log.error("Failed to write JSON", e);
        }
    }
    private static String csv(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n");
        String escaped = s.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }

}
