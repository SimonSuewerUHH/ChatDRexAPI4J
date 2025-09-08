package de.hamburg.university.agent.planning;

import de.hamburg.university.agent.memory.InMemoryStateHolder;
import de.hamburg.university.agent.memory.PlanStateResult;
import de.hamburg.university.agent.planning.bots.HelpBot;
import de.hamburg.university.agent.planning.bots.RequestClassifierBot;
import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.api.chat.messages.ChatMessageType;
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
    HelpBot helpBot;

    @Inject
    InMemoryStateHolder stateHolder;

    @ActivateRequestContext
    public Multi<ChatResponseDTO> answer(ChatRequestDTO content) {
        return Multi.createFrom().emitter(em -> {
            ToolDTO toolDTO = new ToolDTO(Tools.CONTEXT.name());
            toolDTO.setInput("Your question");
            em.emit(ChatResponseDTO.createToolResponse(content, toolDTO));
            List<PlanStateResult> states = stateHolder.getStates(content.getConnectionId());
            RequestClassification classy = requestClassifierBot.classify(content.getMessage(), states);

            toolDTO.setStop();
            toolDTO.addContent("Context:" + classy.getRelevantDiscussion());
            toolDTO.addContent("You need: " + classy.getRoute());
            em.emit(ChatResponseDTO.createToolResponse(content, toolDTO));

            RequestRoute route = RequestRoute.from(classy.getRoute());
            Log.infof("Classified %s request as: %s", content, route);
            AgentResult result = answer(content, classy.getRelevantDiscussion(), RequestRoute.UNKNOWN, em);

            em.emit(ChatResponseDTO.createSingleResponse(content, result.getMessageMarkdown(), ChatMessageType.AI));

            em.emit(ChatResponseDTO.createAPIResponse(content, "Stop"));
            em.complete();
        });
    }

    private AgentResult answer(ChatRequestDTO content, String context, RequestRoute route, MultiEmitter<? super ChatResponseDTO> emitter) {
        return switch (route) {
            case HELP -> new AgentResult(helpBot.answer(content.getMessage()));
            default -> planningAgent.planAnswer(content, context, emitter);
        };
    }
}
