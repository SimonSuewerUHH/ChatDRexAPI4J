package de.hamburg.university.service.uniprotkb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneTextDTO {
    private String value;
}
