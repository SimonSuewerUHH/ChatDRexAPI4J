package de.hamburg.university.agent.provider.supplier;

import de.hamburg.university.agent.provider.LanguageModelProvider;
import de.hamburg.university.agent.provider.setting.UserLLMModelSetting;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.enterprise.inject.spi.CDI;

import java.util.function.Supplier;

public class ChatLanguageModelSupplier implements Supplier<ChatModel> {

    @Override
    public ChatModel get() {
        UserLLMModelSetting setting = CDI.current().select(UserLLMModelSetting.class).get();
        return LanguageModelProvider.getText(setting);

    }
}
