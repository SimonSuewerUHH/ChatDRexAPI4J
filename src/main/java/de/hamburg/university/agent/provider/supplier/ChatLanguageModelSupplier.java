package de.hamburg.university.agent.provider.supplier;

import de.hamburg.university.agent.provider.LanguageModelProvider;
import de.hamburg.university.agent.provider.setting.UserLLMModelSetting;
import dev.langchain4j.model.chat.ChatModel;

import java.util.function.Supplier;

public class ChatLanguageModelSupplier implements Supplier<ChatModel> {

    @Override
    public ChatModel get() {
        return LanguageModelProvider.getText(new UserLLMModelSetting());

    }
}
