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
    private UserLLMModelSettingDTO settings;

    public UserLLMModelSetting() {
        Config configProvider = ConfigProvider.getConfig();
        this.settings = SETTINGS.get();
        this.ollamaApiKey = configProvider.getValue("quarkus.langchain4j.openai.api-key", String.class);
        this.ollamaBaseUrl = configProvider.getValue("quarkus.langchain4j.openai.base-url", String.class);
        this.logResponses = configProvider.getValue("quarkus.langchain4j.openai.chat-model.log-responses", Boolean.class);
        this.logRequests = configProvider.getValue("quarkus.langchain4j.openai.chat-model.log-requests", Boolean.class);
        this.chatGPTModelName = configProvider.getValue("quarkus.langchain4j.openai.chat-model.model-name", String.class);
        this.geminiModelName = configProvider.getValue("quarkus.langchain4j.gemini.chat-model.model-name", String.class);
    }


    public String getChatGPTModelName() {
        return Optional.ofNullable(this.settings.getChatGptModel())
                .filter(StringUtils::isNotEmpty)
                .orElse(chatGPTModelName);
    }

    public String getChatGptApiKey() {
        return Optional.ofNullable(this.settings.getChatGptApiKey())
                .filter(StringUtils::isNotEmpty)
                .orElseGet(() -> {
                    if (this.settings.getSelectedLLM() == UserLLMType.OLLAMA && this.settings.getOllamaBaseUrl().equals(ollamaBaseUrl)) {
                        return ollamaApiKey;
                    }
                    return "API_KEY_MISSING";
                });
    }

    public String getGeminiApiKey() {
        return Optional.ofNullable(this.settings.getGeminiApiKey())
                .filter(StringUtils::isNotEmpty)
                .orElse("API_KEY_MISSING");
    }

    public String getGeminiModel() {
        return Optional.ofNullable(this.settings.getGeminiModel())
                .filter(StringUtils::isNotEmpty)
                .orElse(geminiModelName);
    }


    public UserLLMType getUserLLMType() {
        return Optional.ofNullable(this.settings.getSelectedLLM()).orElse(UserLLMType.OLLAMA);
    }

    public String getChatModelName() {
        return switch (getUserLLMType()) {
            case CHATGPT, OLLAMA -> getChatGPTModelName();
            case GEMINI -> getGeminiModel();
        };
    }

}
