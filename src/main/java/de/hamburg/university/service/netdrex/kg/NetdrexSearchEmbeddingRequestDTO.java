package de.hamburg.university.service.netdrex.kg;

import lombok.Data;

@Data
public class NetdrexSearchEmbeddingRequestDTO {
    private String query;
    private String collection;
    private Integer top;


}
