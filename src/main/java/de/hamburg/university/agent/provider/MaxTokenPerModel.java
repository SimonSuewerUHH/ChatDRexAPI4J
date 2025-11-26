package de.hamburg.university.agent.provider;

import lombok.Getter;

public enum MaxTokenPerModel {
    MISTRAL_7B("mistral:7b", 32768),
    GEMMA2_9B("gemma2:9b", 8192),
    LLAMA3_2_LATEST("llama3.2:latest", 131072),
    GEMMA2_27B("gemma2:27b", 8192),
    SMOLLM2_LATEST("smollm2:latest", 8192),
    QWEN2_5_LATEST("qwen2.5:latest", 32768),
    GPT_4O_MINI("gpt-4o-mini", 128000),
    GPT_4O("gpt-4o", 128000),
    GEMINI_2_0_FLASH("gemini-2.0-flash", 1000000),
    GEMINI_1_5_FLASH("gemini-1.5-flash", 1000000),
    GEMINI_1_5_FLASH_8B("gemini-1.5-flash-8b", 100000);

    private final String value;
    @Getter
    private final int maxTokens;

    MaxTokenPerModel(String value, int maxTokens) {
        this.value = value;
        this.maxTokens = maxTokens;
    }

    public static MaxTokenPerModel fromValue(String value) {
        for (MaxTokenPerModel model : values()) {
            if (model.value.equals(value)) {
                return model;
            }
        }
        throw new IllegalArgumentException("Unknown model value: " + value);
    }
}