package de.hamburg.university.agent.provider.supplier;

import de.hamburg.university.agent.provider.LanguageModelProvider;
import de.hamburg.university.agent.provider.setting.UserLLMModelSetting;
import de.hamburg.university.agent.provider.setting.UserLLMModelSettingDTO;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.enterprise.inject.spi.CDI;

import java.util.function.Supplier;

import static de.hamburg.university.agent.provider.supplier.ChatJsonLanguageModelSupplier.SETTINGS;

public class ChatLanguageModelSupplier implements Supplier<ChatModel> {

    @Override
    public ChatModel get() {
        UserLLMModelSetting setting = CDI.current().select(UserLLMModelSetting.class).get();
        UserLLMModelSettingDTO threadSetting = SETTINGS.get();
        if (threadSetting != null) {
            setting.setUserSetting(threadSetting);
        }
        return LanguageModelProvider.getText(setting);

    }
}
