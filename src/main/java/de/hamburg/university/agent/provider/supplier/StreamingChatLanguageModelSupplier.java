package de.hamburg.university.agent.provider.supplier;

import de.hamburg.university.agent.provider.LanguageModelProvider;
import de.hamburg.university.agent.provider.setting.UserLLMModelSetting;
import dev.langchain4j.model.chat.StreamingChatModel;

import java.util.function.Supplier;

public class StreamingChatLanguageModelSupplier implements Supplier<StreamingChatModel> {

    @Override
    public StreamingChatModel get() {
        return LanguageModelProvider.getStreamingText(new UserLLMModelSetting());
    }
}
