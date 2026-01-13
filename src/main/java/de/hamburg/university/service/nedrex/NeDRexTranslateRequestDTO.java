package de.hamburg.university.service.nedrex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NeDRexTranslateRequestDTO {
    @JsonProperty("nodes")
    private List<String> nodes;
}

