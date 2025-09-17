package de.hamburg.university.agent.guardrails;

import de.hamburg.university.ChatdrexConfig;
import de.hamburg.university.agent.bot.guardrails.ParagraphGroundingScorer;
import de.hamburg.university.agent.planning.PlanState;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GroundingOutputGuardrail implements OutputGuardrail {

    @Inject
    ParagraphGroundingScorer scorer;

    @Inject
    ChatdrexConfig config;

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        return OutputGuardrailResult.success();
    }

    @Override
    public OutputGuardrailResult validate(OutputGuardrailRequest request) {
        if (!config.guardtrails().grounding().enabled()) {
            return OutputGuardrailResult.success();
        }
        String paragraph = request.responseFromLLM().aiMessage().text();
        if (paragraph == null || paragraph.isBlank()) {
            return OutputGuardrailResult.success();
        }

        PlanState stateObj = (PlanState) request.requestParams().variables().get("state");

        double score = scorer.score(paragraph, stateObj);

        if (score < config.guardtrails().grounding().score().threshold()) {
            return retry(
                    "Paragraph flagged as likely ungrounded/invalid (score=" + format(score) + "). " +
                            "Re-write this paragraph to strictly follow the contract: " +
                            "ground every claim in the given PlanState evidence and fix structure/citations."
            );
        }

        return OutputGuardrailResult.success();
    }


    private static String format(double d) {
        return String.format(java.util.Locale.ROOT, "%.2f", d);
    }
}