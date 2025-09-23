package de.hamburg.university.tool.pojo;

import lombok.Data;

@Data
public class NeDRexToolTestResult {
    private String question;
    private String path;
    private boolean correctTool;
    private boolean correctInput;
    private boolean correctCall;
    private boolean correctAnswer;

    public NeDRexToolTestResult(String question, String path) {
        this.question = question;
        this.path = path;
        this.correctTool = false;
        this.correctInput = false;
        this.correctCall = false;
        this.correctAnswer = false;
    }
}
