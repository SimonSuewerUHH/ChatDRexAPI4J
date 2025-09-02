package de.hamburg.university.service.research.semanticscholar;

import lombok.Data;

import java.util.List;

@Data
public class SemanticScholarResponseDTO {
    private String total;
    private String token;
    private List<SemanticScholarPaperDTO> data;
}
