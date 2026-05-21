package de.hamburg.university.agent.provider;

import de.hamburg.university.agent.provider.setting.UserLLMModelSetting;
import de.hamburg.university.agent.provider.setting.UserLLMType;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class LanguageModelProvider {

    //OLLAMA
    private static OpenAiChatModel.OpenAiChatModelBuilder ollamaModelBuilder(UserLLMModelSetting setting) {
        OpenAiChatModel.OpenAiChatModelBuilder builder = openAiModelBuilder(setting);
        return builder.baseUrl(setting.getOllamaBaseUrl())
                .apiKey(setting.getOllamaApiKey())
                .customHeaders(
                        Map.of("Authorization", "Bearer " + setting.getOllamaApiKey())
                );
    }

    private static OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder ollamaModelStreamingBuilder(UserLLMModelSetting setting) {
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = openAiModelStreamingBuilder(setting);
        return builder.baseUrl(setting.getOllamaBaseUrl())
                .apiKey(setting.getOllamaApiKey())
                .customHeaders(
                        Map.of("Authorization", "Bearer " + setting.getOllamaApiKey())
                );
    }

    //CHATGPT
    private static OpenAiChatModel.OpenAiChatModelBuilder openAiModelBuilder(UserLLMModelSetting setting) {

        return OpenAiChatModel.builder()
                .apiKey(setting.getChatGptApiKey())
                .defaultRequestParameters(ChatRequestParameters.builder()
                        .modelName(setting.getChatGPTModelName())
                        .build())
                .logRequests(setting.isLogRequests())
                .maxRetries(3)
                .logResponses(setting.isLogResponses());

    }

    private static OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder openAiModelStreamingBuilder(UserLLMModelSetting setting) {

        return OpenAiStreamingChatModel.builder()
                .apiKey(setting.getChatGptApiKey())
                .defaultRequestParameters(ChatRequestParameters.builder()
                        .modelName(setting.getChatGPTModelName())
                        .build())
                .logRequests(setting.isLogRequests())
                .logResponses(setting.isLogResponses());
    }


    //GOOGLE Gemini
    //https://docs.langchain4j.dev/integrations/language-models/google-ai-gemini/#multimodality
    private static GoogleAiGeminiChatModel.GoogleAiGeminiChatModelBuilder googleGeminiModelBuilder(UserLLMModelSetting setting) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(setting.getGeminiApiKey())
                .modelName(setting.getGeminiModel())
                .timeout(Duration.of(50, ChronoUnit.SECONDS))
                .logRequests(setting.isLogRequests())
                .logResponses(setting.isLogResponses());
    }

    private static GoogleAiGeminiStreamingChatModel.GoogleAiGeminiStreamingChatModelBuilder googleGeminiStreamingBuilder(UserLLMModelSetting setting) {
        return GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(setting.getGeminiApiKey())
                .modelName(setting.getGeminiModel())
                .timeout(Duration.of(50, ChronoUnit.SECONDS))
                .logRequests(setting.isLogRequests())
                .logResponses(setting.isLogResponses());
    }


    public static ChatModel getJson(UserLLMModelSetting setting) {
        if (setting.getUserLLMType().equals(UserLLMType.OLLAMA)) {
            return ollamaModelBuilder(setting)
                    .logRequests(true)
                    .logResponses(true)
                    .responseFormat(ResponseFormat.JSON)
                    .build();
        } else if (setting.getUserLLMType().equals(UserLLMType.GEMINI)) {
            return googleGeminiModelBuilder(setting)
                    .responseFormat(ResponseFormat.JSON)
                    .build();
        } else {
            return openAiModelBuilder(setting)
                    .responseFormat("json_schema")
                    .strictJsonSchema(true)
                    .build();
        }
    }

    public static ChatModel getText(UserLLMModelSetting setting) {
        if (setting.getUserLLMType().equals(UserLLMType.OLLAMA)) {
            return ollamaModelBuilder(setting)
                    .timeout(Duration.of(50, ChronoUnit.SECONDS))
                    .build();
        } else if (setting.getUserLLMType().equals(UserLLMType.GEMINI)) {
            return googleGeminiModelBuilder(setting)
                    .timeout(Duration.of(50, ChronoUnit.SECONDS))
                    .responseFormat(null)
                    .build();
        } else {
            return openAiModelBuilder(setting)
                    .build();
        }
    }

    public static StreamingChatModel getStreamingJson(UserLLMModelSetting setting) {
        if (setting.getUserLLMType().equals(UserLLMType.OLLAMA)) {
            return ollamaModelStreamingBuilder(setting)
                    .responseFormat("json_schema")
                    .strictJsonSchema(true)
                    .build();
        } else if (setting.getUserLLMType().equals(UserLLMType.GEMINI)) {
            return googleGeminiStreamingBuilder(setting)
                    .responseFormat(ResponseFormat.JSON)
                    .build();
        } else {
            return openAiModelStreamingBuilder(setting)
                    .responseFormat("json_schema")
                    .strictJsonSchema(true)
                    .build();
        }

    }

    public static StreamingChatModel getStreamingText(UserLLMModelSetting setting) {
        if (setting.getUserLLMType().equals(UserLLMType.OLLAMA)) {
            return ollamaModelStreamingBuilder(setting)
                    .build();
        } else if (setting.getUserLLMType().equals(UserLLMType.GEMINI)) {
            return googleGeminiStreamingBuilder(setting)
                    .build();
        } else {
            return openAiModelStreamingBuilder(setting)
                    .build();
        }
    }
}
