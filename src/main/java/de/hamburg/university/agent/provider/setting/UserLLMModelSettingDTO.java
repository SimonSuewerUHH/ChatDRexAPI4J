package de.hamburg.university.agent.provider.setting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserLLMModelSettingDTO {
    private UserLLMType selectedLLM;
    private String ollamaBaseUrl;
    private String ollamaApiKey;

    private String chatGptModel;
    private String chatGptApiKey;

    private String geminiModel;
    private String geminiApiKey;

    // Advanced model settings
    private Double temperature;
    private Integer topK;
    private Double topP;
    private Double repeatPenalty;
    private Long seed;
    private Integer numPredict;
    private Integer numCtx;

    //QueryKeys
    private String semanticScholarApiKey;
}
