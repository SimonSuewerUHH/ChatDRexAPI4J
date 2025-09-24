package de.hamburg.university.tool.pojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.hamburg.university.agent.tool.research.ToolSourceDTO;
import io.quarkus.logging.Log;
import lombok.Data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
public class ResearchResult {
    private String question;
    private String answer;
    List<ToolSourceDTO> searchResults = new ArrayList<>();
    private boolean correctAnswer;


    @Override
    public String toString() {
        return "NeDRexToolTestResult " +
                "question=" + question + '\n' +
                ", answer=" + answer + '\n' +
                ", searchResults=" + searchResults.size() + '\n' +
                ", correctAnswer=" + correctAnswer + '\n' +
                "_________________________________________";
    }

    public static void printJsonFile(List<ResearchResult> rows, Path file) {
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
