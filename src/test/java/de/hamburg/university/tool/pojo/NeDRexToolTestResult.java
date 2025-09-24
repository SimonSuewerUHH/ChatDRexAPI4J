package de.hamburg.university.tool.pojo;

import lombok.Data;

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
}
