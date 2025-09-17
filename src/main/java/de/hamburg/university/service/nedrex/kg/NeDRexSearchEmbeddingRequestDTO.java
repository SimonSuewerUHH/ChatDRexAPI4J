package de.hamburg.university.service.nedrex.kg;

import lombok.Data;

@Data
public class NeDRexSearchEmbeddingRequestDTO {
    private String query;
    private String collection;
    private Integer top;


}
