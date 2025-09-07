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
            }
            
            ---
            
            Available actions:
            - **UPDATE_NETWORK** → when network asks for highlight specific parts of it.
            - **FETCH_RESEARCH** → When the user asks for background information or the current answer could use more information.
            - **FETCH_KG** → when knowledge-graph context is needed. 
            This should be preferred for a question if the question is aimed at obtaining information that could be included in a knowledge graph.
            This is NOT the case if the user ask for DIAMOND, trustrank, Closess or enrichment analysis.
            - **FETCH_BIO_INFO** → when biological enrichment of the query is needed.
            - **CALL_NETDREX_TOOL** → when a user asks for diamond, trustrank, closeness or just drug repurposing. 
            If the user ask for running these tools like Only run DIAMOND on my seeds, you should call this action directly if the needed context is already available.
            Here you dont need to fetch more context.
            - **CALL_DIGEST_TOOL** → when enrichment analysis is needed.
            Here you dont need to fetch more context.
            - **FINALIZE** → when you can summarize and recommend next steps.
            
            ---
            
            Output policy:
            - Output ONLY a valid JSON object with fields: action, reason
            ---
            
            ### Examples
            
            1. Need to update network with new seeds:
            {
              "action": "UPDATE_NETWORK",
              "reason": "User wanna have drugs red"
            }
            
            3. Fetch research:
            {
              "action": "FETCH_RESEARCH",
              "reason": "User asked for supporting literature on TP53"
            }
            
            4. Fetch KG context:
            {
              "action": "FETCH_KG",
              "reason": "Need Netdrex KG context before algorithm run"
            }
            
            5. Fetch biological info:
            {
              "action": "FETCH_BIO_INFO",
              "reason": "Query ambiguous, need enhanced bio info"
            }
            
            6. Call Netdrex tool:
            {
              "action": "CALL_NETDREX_TOOL",
              "reason": "Netdrex algorithm requested with KG context available"
            }
            
            7. Call Digest tool:
            {
              "action": "CALL_DIGEST_TOOL",
              "reason": "Perform enrichment analysis on provided seed set"
            }
            
            8. Finalize with recommendation:
            {
              "action": "FINALIZE",
              "reason": "All context gathered; providing summary"
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
            - If every required action is already in history, immediately return a `FINALIZE` step.
            - If the user asked for Netdrex tool (diamond, trustrank, closeness) and you have seeds, call it DIRECTLY WITHOUT using kg information.
            - If the user asked for enrichment analysis and you have seeds, call it DIRECTLY WITHOUT using kg information.
            
            Please decide the next step now.
            """)
    PlanStep decide(PlanState state, List<PlanStep> history, int stepsLeft);
}