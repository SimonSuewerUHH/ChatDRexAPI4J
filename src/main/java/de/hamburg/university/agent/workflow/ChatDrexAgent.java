package de.hamburg.university.agent.workflow;

import de.hamburg.university.agent.workflow.bots.HelpBot;
import de.hamburg.university.api.chat.messages.ChatMessageType;
import de.hamburg.university.api.chat.messages.ChatRequestDTO;
import de.hamburg.university.api.chat.messages.ChatResponseDTO;
import io.smallrye.mutiny.Multi;
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
            AgentResult result = answer(content.getMessage(), RequestRoute.UNKNOWN, 1L);

            em.emit(ChatResponseDTO.createSingleResponse(content, result.getMessageMarkdown(), ChatMessageType.AI));

            em.emit(ChatResponseDTO.createAPIResponse(content, "Stop"));
            em.complete();
        });
    }

    private AgentResult answer(String content, RequestRoute route, Long workflowId) {
        return switch (route) {
            case HELP -> new AgentResult(helpBot.answer(content));
            default -> planningAgent.planAnswer(workflowId, content);
        };
    }
}
