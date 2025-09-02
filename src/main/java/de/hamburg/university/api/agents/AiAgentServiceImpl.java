package de.hamburg.university.api.agents;

import de.hamburg.university.agent.bot.NetdrexBot;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AiAgentServiceImpl implements AIAgentService {
    @Inject
    NetdrexBot netdrexBot;

    @Override
    public String ask(String question) {
        return netdrexBot.answer(question);
    }
}
