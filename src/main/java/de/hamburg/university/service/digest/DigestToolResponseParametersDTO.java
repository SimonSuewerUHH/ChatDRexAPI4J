package de.hamburg.university.service.digest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigestToolResponseParametersDTO {
    @JsonProperty("target_id")
    private String targetId;
    private List<String> target;
    private Integer runs;
    private Integer replace;
    private String distance;
    @JsonProperty("background_model")
    private String backgroundModel;
    private String type;
}