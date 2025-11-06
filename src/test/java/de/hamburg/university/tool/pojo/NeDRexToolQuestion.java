package de.hamburg.university.tool.pojo;

import lombok.Data;

@Data
public class NeDRexToolQuestion {
    private String question;
    private String steps;
    private String content;
    private String path;
}
