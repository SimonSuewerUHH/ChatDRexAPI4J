package de.hamburg.university.tool.pojo;

import lombok.Data;

import java.util.Map;

@Data
public class DrugstOneConfigQuestion {
    private String question;
    private Map<String, Object> partResult;
}
