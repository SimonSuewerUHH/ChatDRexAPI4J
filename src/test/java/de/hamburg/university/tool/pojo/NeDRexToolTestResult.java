package de.hamburg.university.tool.pojo;

import lombok.Data;

@Data
public class NeDRexToolTestResult {
    private boolean correctTool;
    private boolean correctInput;
    private boolean correctCall;
    private boolean correctAnswer;

    public NeDRexToolTestResult() {
        this.correctTool = false;
        this.correctInput = false;
        this.correctCall = false;
        this.correctAnswer = false;
    }
}
