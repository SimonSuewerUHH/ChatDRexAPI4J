package de.hamburg.university.agent.provider.setting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
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


    public static UserLLMModelSettingDTO getFromString(String userSettingBase64) {

        if (StringUtils.isEmpty(userSettingBase64)) {
            return new UserLLMModelSettingDTO();
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            String userSetting = new String(java.util.Base64.getDecoder().decode(userSettingBase64));
            return mapper.readValue(userSetting, UserLLMModelSettingDTO.class);
        } catch (JsonProcessingException e) {
            Log.warn("Error while parsing user setting", e);
        }
        return new UserLLMModelSettingDTO();
    }
}
