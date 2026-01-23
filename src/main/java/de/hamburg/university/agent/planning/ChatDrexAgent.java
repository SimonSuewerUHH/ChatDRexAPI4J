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
import org.eclipse.microprofile.context.ManagedExecutor;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class ChatDrexAgent {

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    PlanningAgent planningAgent;

    @Inject
    RequestClassifierBot requestClassifierBot;

    @Inject
    InMemoryStateHolder stateHolder;

    @ActivateRequestContext
    public Multi<ChatResponseDTO> answer(ChatRequestDTO content, UserLLMModelSettingDTO settings) {
        return Multi.createFrom().emitter(em -> {
            final AtomicBoolean terminated = new AtomicBoolean(false);
            em.onTermination(() -> terminated.set(true));

            managedExecutor.execute(() -> {
                ChatJsonLanguageModelSupplier.SETTINGS.set(settings);
                try {
                    String context = getContext(content, em);
                    AgentResult result = planningAgent.planAnswer(content, context, em, terminated);
                    Log.infof("Final result length: %d", result.getMessageMarkdown().length());
                    //em.emit(ChatResponseDTO.createSingleResponse(content, result.getMessageMarkdown(), ChatMessageType.AI));

                    em.emit(ChatResponseDTO.createAPIResponse(content, "Stop"));
                    ChatJsonLanguageModelSupplier.SETTINGS.remove();
                    em.complete();
                } catch (Throwable t) {
                    Log.error("Unhandled error in answer() stream", t);
                    em.emit(ChatResponseDTO.createErrorResponse(content, t.getMessage()));
                } finally {
                    ChatJsonLanguageModelSupplier.SETTINGS.remove();
                    if (!terminated.get()) em.complete();
                }
            });
        });
    }

    private String getContext(ChatRequestDTO content, MultiEmitter<? super ChatResponseDTO> em) {
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
        return context;
    }
}
