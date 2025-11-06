package de.hamburg.university.tool.pojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.logging.Log;
import lombok.Data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
public class NeDRexToolTestResult {
    private String question;
    private String path;
    private boolean correctTool;
    private boolean correctInput;
    private boolean correctAnswer;
    private List<String> missingInputs;

    public NeDRexToolTestResult(String question, String path) {
        this.question = question;
        this.path = path;
        this.correctTool = false;
        this.correctInput = false;
        this.correctAnswer = false;
    }

    public void addMissingInput(String input) {
        if (missingInputs == null) {
            missingInputs = new ArrayList<>();
        }
        missingInputs.add(input);
    }

    @Override
    public String toString() {
        return "NeDRexToolTestResult " +
                "question=" + question + '\n' +
                ", correctTool=" + correctTool + '\n' +
                ", correctInput=" + correctInput + '\n' +
                ", correctAnswer=" + correctAnswer + '\n' +
                (missingInputs != null ? ", falseInputs=" + missingInputs + '\n' : "") +
                "_________________________________________";
    }

    public static void printJsonFile(List<NeDRexToolTestResult> rows, Path file) {
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

    public static List<NeDRexToolTestResult> loadJsonFile(Path file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            return mapper.readValue(
                    file.toFile(),
                    mapper.getTypeFactory().constructCollectionType(List.class, NeDRexToolTestResult.class)
            );
        } catch (Exception e) {
            // include the absolute file path in the log and attach the exception
            Log.errorf(e, "Failed to read JSON from %s", file.toAbsolutePath());
            return null;
        }
    }
}
