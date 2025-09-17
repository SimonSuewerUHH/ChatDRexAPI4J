package de.hamburg.university.agent.bot.guardrails;

import de.hamburg.university.agent.planning.PlanState;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
@ApplicationScoped
public interface ParagraphGroundingScorer {

    @SystemMessage("""
            You are a strict hallucination and structure detector for streamed markdown paragraphs.
            Output ONLY a floating point number between 0.0 and 1.0 (no text, no newline).
            
            The score measures how likely the paragraph is UNGROUNDED or STRUCTURALLY NON-COMPLIANT
            relative to the provided PlanState and formatting rules:
            
            Grounding rules (use PlanState fields ONLY as sources of truth):
            - All factual claims must be supported by content from PlanState.research, nedrexKgInfo,
              digestResult, enhancedQueryBioInfo, or drugstOneNetwork.
            - If the paragraph introduces facts/entities not present in PlanState, treat as hallucination.
            
            Structure rules (markdown contract):
            - No H1; headings start with '## '.
            - Each factual sentence ends with bracketed citations like [paper123]; multiple allowed.
            - If drugbank/uniprot/entrez IDs appear, they must be tagged as:
              [drugBank:DBXXXX], [uniProt:PXXXXX], [entrez:NNNN].
            - Markdown only (no top-level JSON as the payload).
            
            Scoring rubric:
            - 0.00–0.10: Fully grounded and perfectly structured.
            - 0.20–0.40: Minor issues (e.g., a sentence missing a citation OR small formatting slip).
            - 0.50–0.70: Clear structural problems or weak grounding (entities present but not cited).
            - 0.80–1.00: Likely hallucinated (claims/entities absent from PlanState) or major format breach.
            
            Return ONLY the number (e.g., 0.65). Do not include any extra text or newline.
            """)
    @UserMessage("""
            Paragraph to evaluate (markdown):
            {paragraph}

            PlanState (JSON):
            {state}

            Return a single float (0.0–1.0) as described. Nothing else.
            """)
    double score(@V("paragraph") String paragraph, PlanState state);
}