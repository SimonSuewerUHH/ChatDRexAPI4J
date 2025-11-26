package de.hamburg.university.agent.provider.setting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Optional;

@RequestScoped
public class UserLLMModelSetting {
    @Inject
    Logger log;

    @Inject
    @Getter
    WebSocketConnection connection;

    @Getter
    @ConfigProperty(name = "quarkus.langchain4j.openai.api-key", defaultValue = "sk-80f5d24ea1ab4bc8ba721a57e4ccd7a8")
    String ollamaApiKey;

    @ConfigProperty(name = "quarkus.langchain4j.openai.base-url", defaultValue = "https://llm.cosy.bio")
    String ollamaBaseUrl;

    @ConfigProperty(name = "quarkus.langchain4j.openai.chat-model.log-responses", defaultValue = "true")
    @Getter
    boolean logResponses;

    @ConfigProperty(name = "quarkus.langchain4j.openai.chat-model.log-requests", defaultValue = "true")
    @Getter
    boolean logRequests;

    @ConfigProperty(name = "quarkus.langchain4j.openai.chat-model.model-name", defaultValue = "gpt-oss:latest")
    String chatGPTModelName;

    @ConfigProperty(name = "quarkus.langchain4j.gemini.chat-model.model-name", defaultValue = "gemini-2.0-flash")
    String geminiModelName;


    @Getter
    private UserLLMModelSettingDTO userSetting = new UserLLMModelSettingDTO();

    public String getChatGPTModelName() {
        return Optional.ofNullable(userSetting.getChatGptModel())
                .filter(StringUtils::isNotEmpty)
                .orElse(chatGPTModelName);
    }


    public String getOllamaBaseUrl() {
        return Optional.ofNullable(userSetting.getOllamaBaseUrl())
                .filter(StringUtils::isNotEmpty)
                .orElse(ollamaBaseUrl);
    }

    public String getChatGptApiKey() {
        return Optional.ofNullable(userSetting.getChatGptApiKey())
                .filter(StringUtils::isNotEmpty)
                .orElseGet(() -> {
                    if (userSetting.getSelectedLLM() == UserLLMType.OLLAMA && userSetting.getOllamaBaseUrl().equals(ollamaBaseUrl)) {
                        return ollamaApiKey;
                    }
                    return "API_KEY_MISSING";
                });
    }

    public String getGeminiApiKey() {
        return Optional.ofNullable(userSetting.getGeminiApiKey())
                .filter(StringUtils::isNotEmpty)
                .orElse("API_KEY_MISSING");
    }

    public String getGeminiModel() {
        return Optional.ofNullable(userSetting.getGeminiModel())
                .filter(StringUtils::isNotEmpty)
                .orElse(geminiModelName);
    }


    public UserLLMType getUserLLMType() {
        return Optional.ofNullable(userSetting.getSelectedLLM()).orElse(UserLLMType.OLLAMA);
    }

    public String getChatModelName() {
        return switch (getUserLLMType()) {
            case CHATGPT, OLLAMA -> getChatGPTModelName();
            case GEMINI -> getGeminiModel();
        };
    }

    public void setUserSetting(UserLLMModelSettingDTO setting) {
        this.userSetting = setting;
    }

    public void setUserSetting(String userSettingBase64) {
        if (StringUtils.isEmpty(userSettingBase64)) {
            this.userSetting = new UserLLMModelSettingDTO();
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            String userSetting = new String(java.util.Base64.getDecoder().decode(userSettingBase64));
            this.userSetting = mapper.readValue(userSetting, UserLLMModelSettingDTO.class);
        } catch (JsonProcessingException e) {
            log.warn("Error while parsing user setting", e);
        }
    }

}
