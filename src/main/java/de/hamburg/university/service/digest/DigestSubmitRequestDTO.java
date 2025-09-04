package de.hamburg.university.service.digest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DigestSubmitRequestDTO {
    @JsonProperty("target_id")
    private String targetId = "entrez";
    private List<String> target;
    private Integer runs = 1000;
    private Integer replace = 100;
    private String distance = "jaccard";
    @JsonProperty("background_model")
    private String backgroundModel;
    private String type = "gene";

    public static DigestSubmitRequestDTO forModel(List<String> target) {
        DigestSubmitRequestDTO request = new DigestSubmitRequestDTO();
        request.setTarget(target);
        return request;
    }

    public static DigestSubmitRequestDTO forSubnetwork(List<String> target) {
        DigestSubmitRequestDTO request = DigestSubmitRequestDTO.forModel(target);
        request.setBackgroundModel("network");
        return request;
    }

    public static DigestSubmitRequestDTO forSet(List<String> target) {
        DigestSubmitRequestDTO request = DigestSubmitRequestDTO.forModel(target);
        request.setBackgroundModel("complete");
        return request;
    }
}
