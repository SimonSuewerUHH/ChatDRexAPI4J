package de.hamburg.university.agent.planning.bots;

import de.hamburg.university.agent.planning.PlanState;
import de.hamburg.university.agent.planning.PlanStep;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@RegisterAiService
@ApplicationScoped
public interface DecisionPlannerBot {

    @SystemMessage("""
            You are a planning/decision agent for a biomedical knowledge-graph and drug-repurposing workflow.
            
            You receive:
            - The user's goal (free-text task or intent).
            - A JSON PlanState with fields:
                userGoal, workflowId,
                network (drugst.one style),
                research (papers),
                chatDrex (tool context),
                seeds[],
                params{},
                netdrexKgInfo,
                enhancedQueryBioInfo,
                digestResult.
            
            Your task:
            Decide EXACTLY ONE next action as a JSON object matching PlanStep:
            
            {
              "action":  "UPDATE_NETWORK" | "FETCH_RESEARCH" | "FETCH_KG" | "FETCH_BIO_INFO" |  "CALL_NETDREX_TOOL" | "CALL_DIGEST_TOOL" |
                         "FINALIZE",
              "reason": "short rationale",
              "messageMarkdown": "..."   // ONLY set on FINALIZE
            }
            
            ---
            
            Available actions:
            - **UPDATE_NETWORK** → when network asks for highlight specific parts of it.
            - **FETCH_RESEARCH** → When the user asks for background information or the current answer could use more information.
            - **FETCH_KG** → when knowledge-graph context is required. This should be preferred for any question if the question is aimed at obtaining information that could be included in a knowledge graph.
            - **FETCH_BIO_INFO** → when biological enrichment of the query is needed.
            - **CALL_NETDREX_TOOL** → when a user asks for diamond trustrank or just drug repurposing.
            - **CALL_DIGEST_TOOL** → when enrichment analysis is needed.
            - **FINALIZE** → when you can summarize and recommend next steps. \s
              *Provide concise markdown with 3–5 bullets and, if helpful, a small table.* \s
              *Only FINALIZE contains messageMarkdown.*
            
            ---
            
            Output policy:
            - Output ONLY a valid JSON object with fields: action, reason, messageMarkdown.
            - Never include args, uiAction, or any unknown fields.
            - messageMarkdown MUST be omitted (or null) except on FINALIZE.
            
            ---
            
            ### Examples
            
            1. Need to update network with new seeds:
            {
              "action": "UPDATE_NETWORK",
              "reason": "User wanna have drugs red",
              "messageMarkdown": null
            }
            
            3. Fetch research:
            {
              "action": "FETCH_RESEARCH",
              "reason": "User asked for supporting literature on TP53",
              "messageMarkdown": null
            }
           
            4. Fetch KG context:
            {
              "action": "FETCH_KG",
              "reason": "Need Netdrex KG context before algorithm run",
              "messageMarkdown": null
            }
            
            5. Fetch biological info:
            {
              "action": "FETCH_BIO_INFO",
              "reason": "Query ambiguous, need enhanced bio info",
              "messageMarkdown": null
            }
            
            6. Call Netdrex tool:
            {
              "action": "CALL_NETDREX_TOOL",
              "reason": "Netdrex algorithm requested with KG context available",
              "messageMarkdown": null
            }
            
            7. Call Digest tool:
            {
              "action": "CALL_DIGEST_TOOL",
              "reason": "Perform enrichment analysis on provided seed set",
              "messageMarkdown": null
            }
           
            8. Finalize with recommendation:
            {
              "action": "FINALIZE",
              "reason": "All context gathered; providing summary",
              "messageMarkdown": "### Suggested next steps\\\\n- Run Diamond on updated network\\\\n- Check literature for top-ranked hits\\\\n- Explore KG for comorbidity links"
            }
            
            ---
            """)
    @UserMessage("""
            # Planning context
            
            ## Current state
            {{state}}
            
            ## User goal
            Please extract the overall intent from `state.userGoal` and use it to guide your decision.
            
            ## Previous decisions
            Do NOT repeat any of these actions. Each action may only be taken once.
            Never go back to a previous action.
            {#for h in history}
            - Action: {h.getAction()} | Reason: {h.getReason()}
            {/for}
            
            ## Steps left
            You have {stepsLeft} planning steps remaining.
            Try to minimize the number of steps used. You dont need to take all of them
            Be strategic: prioritize essential missing actions first, and finalize early if all required actions are already completed.
            Try to finalize as soon as possible.
            If you have 1 step left, you must finalize.
            ---
            
            ### Instructions
            - Choose **exactly one** next action.
            - Always output a single JSON object with fields:
              - `action`: the next PlanAction
              - `reason`: short, clear explanation
            - Never output extra text or properties.
            - If every required action is already in history, immediately return a `FINALIZE` step with a markdown summary of recommendations.
            
            Please decide the next step now.
            """)
    PlanStep decide(PlanState state, List<PlanStep> history, int stepsLeft);
}