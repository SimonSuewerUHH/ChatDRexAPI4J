package de.hamburg.university.agent.planning;

import de.hamburg.university.agent.memory.InMemoryStateHolder;
import de.hamburg.university.agent.memory.PlanStateResult;
import de.hamburg.university.agent.planning.bots.RequestClassifierBot;
import de.hamburg.university.agent.provider.setting.UserLLMModelSettingDTO;
import de.hamburg.university.agent.provider.supplier.ChatJsonLanguageModelSupplier;
import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ChatDrexAgent {


    @Inject
    PlanningAgent planningAgent;

    @Inject
    RequestClassifierBot requestClassifierBot;

    @Inject
    InMemoryStateHolder stateHolder;

    @ActivateRequestContext
    public Multi<ChatResponseDTO> answer(ChatRequestDTO content, UserLLMModelSettingDTO settings) {
        return Multi.createFrom().emitter(em -> {
            ChatJsonLanguageModelSupplier.SETTINGS.set(settings);

            List<PlanStateResult> states = stateHolder.getStates(content.getConnectionId());
            String context = "";
            if (!states.isEmpty()) {
                ToolDTO toolDTO = new ToolDTO(Tools.CONTEXT.name());
                toolDTO.setInput(states);
                em.emit(ChatResponseDTO.createToolResponse(content, toolDTO));
                RequestClassification classy = requestClassifierBot.classify(content.getMessage(), states);
                context = classy.getRelevantDiscussion();
                toolDTO.setStop();
                toolDTO.addContent("Context:" + context);
                em.emit(ChatResponseDTO.createToolResponse(content, toolDTO));
            }


            AgentResult result = answer(content, context, em);
            Log.infof("Final result length: %d", result.getMessageMarkdown().length());
            //em.emit(ChatResponseDTO.createSingleResponse(content, result.getMessageMarkdown(), ChatMessageType.AI));

            em.emit(ChatResponseDTO.createAPIResponse(content, "Stop"));
            ChatJsonLanguageModelSupplier.SETTINGS.remove();
            em.complete();
        });
    }

    private AgentResult answer(ChatRequestDTO content, String context, MultiEmitter<? super ChatResponseDTO> emitter) {
        return planningAgent.planAnswer(content, context, emitter);
    }
}
