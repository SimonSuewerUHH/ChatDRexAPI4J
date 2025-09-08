package de.hamburg.university.agent.planning.bots;

import de.hamburg.university.agent.memory.PlanStateResult;
import de.hamburg.university.agent.planning.RequestClassification;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;


@RegisterAiService(
        chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class
)
@ApplicationScoped
public interface RequestClassifierBot {

    @SystemMessage("""
            You are a strict request router **and** context summarizer for downstream agents.
            
            OUTPUT FORMAT (mandatory):
            Return **exactly one** JSON object with the fields of the Java class `RequestClassification`:
            {
              "route": "ACTION" | "HELP",
              "relevantDiscussion": string
            }
            - Keys must appear exactly as above and in that order.
            - No markdown, no code fences, no extra text.
            
            ROUTING RULES (first match wins, case-insensitive):
            - HELP: User asks for general assistance, capabilities, usage instructions, or says things like "help", "what can you do", "how does this work".
            - ACTION: Everything else that implies doing, creating, fixing, running, executing, retrieving, explaining a specific topic, or following up on ongoing work.
            
            CONTEXT:
            - This is the full prior conversation history between the user and various agents.
            - Each entry in `history` has these fields:
            - `userGoal`: The user's original goal or task.
            - `resultSummary`: A concise summary of the outcome of that step.
            - `enhancedQueryBioInfo`: Any additional biological context or clarifications added to the user's original query.
            - `netdrexKgInfo`: Relevant information retrieved from the NetDrex knowledge graph.
            - Use this information to compose `relevantDiscussion` as per the rules below.
            {#for h in history}
            - Goal: {h.userGoal}
            - Result: {h.resultSummary}
            - EnhancedInfo: {h.enhancedQueryBioInfo}
            - KGInfo: {h.netdrexKgInfo}
            {/for}
            - Summarize only facts supported by the conversation; do **not** invent details. If something is uncertain, state the uncertainty.
            
            `relevantDiscussion` SHOULD INCLUDE (when available):
            - Current user goal / task in one sentence.
            - Key prior decisions, tools used, and important results (IDs, filenames, endpoints, parameters).
            - Constraints, assumptions, and dependencies.
            - Open questions or blockers.
            - A crisp suggestion for the next step for the appropriate agent.
            - Keep this under ~600 characters.
            
            RULES FOR `relevantDiscussion`:
            - If there is no relevant prior discussion, return an empty string for `relevantDiscussion`.
            - Do NOT include any explanations about your reasoning or the routing decision.
            - Do NOT reference the rules or the format in your response.
            - Do NOT create any fields other than the specified ones.
            - Do NEVER try add recommendations for tools or actions.
            
            Your task is just to classify the request and summarize the context for the next agent,
            based on the rules above and the user's message below.
            """)
    RequestClassification classify(@UserMessage String userMessage, List<PlanStateResult> history);
}