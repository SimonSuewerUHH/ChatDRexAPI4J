package de.hamburg.university.helper;

import de.hamburg.university.agent.provider.setting.UserLLMModelSetting;
import de.hamburg.university.agent.provider.setting.UserLLMModelSettingDTO;
import de.hamburg.university.agent.provider.setting.UserLLMType;
import de.hamburg.university.agent.provider.supplier.ChatJsonLanguageModelSupplier;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class EvaluationModelProviders {

    public Collection<DynamicTest> dynamicTests(String name, Executable test) {
        return providers().stream()
                .map(provider -> DynamicTest.dynamicTest(name + " [" + provider + "]", () -> runWithProvider(provider, test)))
                .toList();
    }

    public List<UserLLMType> providers() {
        String configuredProviders = System.getProperty("chatdrex.eval.providers",
                System.getenv().getOrDefault("CHATDREX_EVAL_PROVIDERS", "OLLAMA,GEMINI"));
        return Arrays.stream(configuredProviders.split(","))
                .map(String::trim)
                .filter(provider -> !provider.isEmpty())
                .map(provider -> UserLLMType.valueOf(provider.toUpperCase(Locale.ROOT)))
                .toList();
    }

    public void runWithProvider(UserLLMType provider, Executable test) throws Throwable {
        UserLLMModelSettingDTO settings = new UserLLMModelSettingDTO();
        settings.setSelectedLLM(provider);
        ChatJsonLanguageModelSupplier.SETTINGS.set(settings);
        try {
            Assumptions.assumeTrue(isConfigured(provider),
                    () -> "Skipping " + provider + " because its credentials are not configured");
            Log.infof("Running evaluation with %s (%s)", provider, activeModelName());
            test.execute();
        } finally {
            ChatJsonLanguageModelSupplier.SETTINGS.remove();
        }
    }

    public String activeModelName() {
        return new UserLLMModelSetting().getChatModelName();
    }

    public String resultDirectoryName() {
        UserLLMModelSetting setting = new UserLLMModelSetting();
        String provider = setting.getUserLLMType().name().toLowerCase(Locale.ROOT);
        String model = sanitizePathSegment(setting.getChatModelName().replace(":latest", ""));
        return provider + "-" + model;
    }

    public int evaluationRuns() {
        String configuredRuns = System.getProperty("chatdrex.eval.runs",
                System.getenv().getOrDefault("CHATDREX_EVAL_RUNS", "1"));
        return Math.max(1, Integer.parseInt(configuredRuns));
    }

    private boolean isConfigured(UserLLMType provider) {
        Config configProvider = ConfigProvider.getConfig();
        return switch (provider) {
            case OLLAMA, CHATGPT -> configProvider.getOptionalValue("quarkus.langchain4j.openai.api-key", String.class)
                    .filter(value -> !value.isBlank())
                    .isPresent();
            case GEMINI -> configProvider.getOptionalValue("quarkus.langchain4j.ai.gemini.api-key", String.class)
                    .filter(value -> !value.isBlank())
                    .isPresent();
        };
    }

    private String sanitizePathSegment(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
