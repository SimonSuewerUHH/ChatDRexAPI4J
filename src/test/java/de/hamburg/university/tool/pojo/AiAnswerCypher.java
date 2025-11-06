package de.hamburg.university.tool.pojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.logging.Log;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Data
@AllArgsConstructor
public class AiAnswerCypher {
    private String answer;
    private String context;
    private String cypher;
    private Boolean isCorrect;
    private String question;
    private int attempt;
    private boolean fallback;

    public AiAnswerCypher(String answer, String context, String cypher, int attempt) {
        this.answer = answer;
        this.context = context;
        this.cypher = cypher;
        this.fallback = false;
        this.attempt = attempt;
    }

    public AiAnswerCypher(String answer, String context) {
        this.answer = answer;
        this.context = context;
        this.fallback = true;
    }

    public static void printJsonFile(List<AiAnswerCypher> rows, Path file) {
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

}
