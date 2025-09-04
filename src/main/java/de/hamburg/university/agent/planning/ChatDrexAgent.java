package de.hamburg.university.agent.planning;

import de.hamburg.university.agent.planning.bots.HelpBot;
import de.hamburg.university.api.chat.messages.ChatMessageType;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

@ApplicationScoped
public class ChatDrexAgent {


    @Inject
    PlanningAgent planningAgent;

    @Inject
    HelpBot helpBot;

    @ActivateRequestContext
    public Multi<ChatResponseDTO> answer(ChatRequestDTO content) {
        return Multi.createFrom().emitter(em -> {
            // String classy = requestClassifierBot.classify(request);
            //RequestRoute route = RequestRoute.from(classy);
            // Log.infof("Classified %s request as: %s", content, route);
            AgentResult result = answer(content, RequestRoute.UNKNOWN, em);

            em.emit(ChatResponseDTO.createSingleResponse(content, result.getMessageMarkdown(), ChatMessageType.AI));

            em.emit(ChatResponseDTO.createAPIResponse(content, "Stop"));
            em.complete();
        });
    }

    private AgentResult answer(ChatRequestDTO content, RequestRoute route, MultiEmitter<? super ChatResponseDTO> emitter) {
        return switch (route) {
            case HELP -> new AgentResult(helpBot.answer(content.getMessage()));
            default -> planningAgent.planAnswer(content, emitter);
        };
    }
}
