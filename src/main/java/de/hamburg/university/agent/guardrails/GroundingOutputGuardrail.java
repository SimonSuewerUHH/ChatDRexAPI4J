package de.hamburg.university.agent.guardrails;

import de.hamburg.university.agent.planning.PlanState;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.regex.Pattern;

@ApplicationScoped
public class GroundingOutputGuardrail implements OutputGuardrail {

    private static final Pattern BRACKET_CIT = Pattern.compile("\\[[^\\]]+\\]");

    @Override
    public OutputGuardrailResult validate(OutputGuardrailRequest req) {
        AiMessage ai = req.responseFromLLM().aiMessage();
        String out = ai != null ? ai.text() : "";

        Object stateObj = req.requestParams().variables().get("state");
        boolean hasResearch = false;
        boolean expectNeDRex = false;
        String allowedIds = null;

        if (stateObj instanceof PlanState ps) {
            hasResearch = ps.getResearch() != null && !ps.getResearch().isEmpty();
            expectNeDRex = ps.getNetdrexKgInfo() != null && !ps.getNetdrexKgInfo().isBlank();
            allowedIds = ps.getNetdrexKgInfo();
        }

        if (hasResearch) {
            String[] sentences = out.split("(?<=[.!?])\\s+");
            for (String s : sentences) {
                if (!s.isBlank() && !BRACKET_CIT.matcher(s).find()) {
                    return reprompt(
                            "Each sentence must end with at least one [PaperID] from the provided 'research' list. Add missing citations and try again.",
                            "Please add explicit [PaperID] citations to every sentence, and [NeDRex] when KG info is used."
                    );
                }
            }
        }

        if (expectNeDRex) {
            if (!out.contains("[NeDRex]")) {
                return reprompt(
                        "NeDRex knowledge was used according to context, but [NeDRex] is missing.",
                        "Add [NeDRex] for facts derived from the KG."
                );
            }

            Pattern idPat = Pattern.compile("\\[(drugBank|uniProt|entrez):([\\w:.-]+)\\]");
            var m = idPat.matcher(out);
            while (m.find()) {
                String id = m.group(2);
                if (!allowedIds.contains(id)) {
                    return reprompt(
                            "Output references an entity id not present in context: " + id,
                            "Only cite IDs present in 'drugstOneNetwork'. Remove or replace invalid IDs."
                    );
                }
            }
        }
        return success();
    }
}