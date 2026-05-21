package de.hamburg.university.agent.provider.setting;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Optional;

import static de.hamburg.university.agent.provider.supplier.ChatJsonLanguageModelSupplier.SETTINGS;

@Data
public class UserLLMModelSetting {

    private String ollamaApiKey;
    private String ollamaBaseUrl;
    private boolean logResponses;
    private boolean logRequests;
    private String chatGPTModelName;
    private String geminiModelName;
    private String geminiApiKey;
    private UserLLMModelSettingDTO settings;

    public UserLLMModelSetting() {
        Config configProvider = ConfigProvider.getConfig();
        this.settings = SETTINGS.get();
        this.ollamaApiKey = configProvider.getValue("quarkus.langchain4j.openai.api-key", String.class);
        this.ollamaBaseUrl = configProvider.getValue("quarkus.langchain4j.openai.base-url", String.class);
        this.logResponses = configProvider.getValue("quarkus.langchain4j.openai.chat-model.log-responses", Boolean.class);
        this.logRequests = configProvider.getValue("quarkus.langchain4j.openai.chat-model.log-requests", Boolean.class);
        this.chatGPTModelName = configProvider.getValue("quarkus.langchain4j.openai.chat-model.model-name", String.class);
        this.geminiModelName = configProvider.getOptionalValue("quarkus.langchain4j.ai.gemini.chat-model.model-id", String.class).orElse("gemini-2.0-flash");
        this.geminiApiKey = configProvider.getOptionalValue("quarkus.langchain4j.ai.gemini.api-key", String.class).orElse("API_KEY_MISSING");
    }


    public String getChatGPTModelName() {
        return Optional.ofNullable(this.settings)
                .map(UserLLMModelSettingDTO::getChatGptModel)
                .filter(StringUtils::isNotEmpty)
                .orElse(chatGPTModelName);
    }

    public String getOllamaApiKey() {
        return Optional.ofNullable(this.settings)
                .map(UserLLMModelSettingDTO::getOllamaApiKey)
                .filter(StringUtils::isNotEmpty)
                .orElse(ollamaApiKey);
    }

    public String getOllamaBaseUrl() {
        return Optional.ofNullable(this.settings)
                .map(UserLLMModelSettingDTO::getOllamaBaseUrl)
                .filter(StringUtils::isNotEmpty)
                .orElse(ollamaBaseUrl);
    }

    public String getChatGptApiKey() {
        return Optional.ofNullable(this.settings)
                .map(UserLLMModelSettingDTO::getChatGptApiKey)
                .filter(StringUtils::isNotEmpty)
                .orElseGet(() -> {
                    if (getUserLLMType() == UserLLMType.OLLAMA
                            && Optional.ofNullable(this.settings)
                            .map(UserLLMModelSettingDTO::getOllamaBaseUrl)
                            .filter(getOllamaBaseUrl()::equals)
                            .isPresent()) {
                        return getOllamaApiKey();
                    }
                    return getOllamaApiKey();
                });
    }

    public String getGeminiApiKey() {
        return Optional.ofNullable(this.settings)
                .map(UserLLMModelSettingDTO::getGeminiApiKey)
                .filter(StringUtils::isNotEmpty)
                .orElse(geminiApiKey);
    }

    public String getGeminiModel() {
        return Optional.ofNullable(this.settings)
                .map(UserLLMModelSettingDTO::getGeminiModel)
                .filter(StringUtils::isNotEmpty)
                .orElse(geminiModelName);
    }


    public UserLLMType getUserLLMType() {
        return Optional.ofNullable(this.settings)
                .map(UserLLMModelSettingDTO::getSelectedLLM)
                .orElse(UserLLMType.OLLAMA);
    }

    public String getChatModelName() {
        return switch (getUserLLMType()) {
            case CHATGPT, OLLAMA -> getChatGPTModelName();
            case GEMINI -> getGeminiModel();
        };
    }

}
