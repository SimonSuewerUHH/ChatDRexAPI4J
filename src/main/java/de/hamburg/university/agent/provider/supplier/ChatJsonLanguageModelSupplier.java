package de.hamburg.university.agent.provider.supplier;

import de.hamburg.university.agent.provider.LanguageModelProvider;
import de.hamburg.university.agent.provider.setting.UserLLMModelSetting;
import de.hamburg.university.agent.provider.setting.UserLLMModelSettingDTO;
import dev.langchain4j.model.chat.ChatModel;

import java.util.function.Supplier;

public class ChatJsonLanguageModelSupplier implements Supplier<ChatModel> {

    public static final ThreadLocal<UserLLMModelSettingDTO> SETTINGS = new ThreadLocal<>();

    @Override
    public ChatModel get() {
        return LanguageModelProvider.getJson(new UserLLMModelSetting());
    }
}
