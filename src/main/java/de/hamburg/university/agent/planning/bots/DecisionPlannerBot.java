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
            
            INPUTS YOU RECEIVE:
            - userGoal: The user's current free-text task or intent.
            - previousContext: A compressed summary of earlier discussions and decisions; use this to avoid repeating tools or steps already taken and to ensure continuity.
            - network: A drugst.one style network object (nodes and edges relevant to biomedical graph analysis).
            - research: A set of papers or literature results.
            - nedrexKgInfo: Retrieved information from the NeDRex Knowledge Graph.
            - enhancedQueryBioInfo: Augmented or reformulated bio-information queries.
            - digestResult: Results from Digest tools (set or subnetwork enrichment analyses).
            
            Your task:
            Decide EXACTLY ONE next action as a JSON object matching PlanStep and PREPARE the `subTaskQuestion` which will be the input for the next agent. The `subTaskQuestion` must express a SINGLE, atomic task for the selected action. If the user goal contains multiple requests, BREAK IT DOWN and choose the most essential next atomic sub-task for this step.
            
            JSON schema to output:
            {
              "action":  "UPDATE_NETWORK" | "FETCH_RESEARCH" | "FETCH_KG" | "FETCH_BIO_INFO" | "CALL_NEDREX_TOOL" | "CALL_DIGEST_TOOL" | "FINALIZE",
              "reason": "short rationale",
              "subTaskQuestion": "single, actionable prompt for the next agent"
            }
            
            Rules for `subTaskQuestion`:
            - One task only; avoid conjunctions like "and"/"as well" unless part of a named entity.
            - Be specific and reference available context (e.g., seeds, disease names, identifiers) when known.
            - Keep it concise (\u2264 25 words) and directly executable by the next agent.
            - For "FINALIZE", set `subTaskQuestion` to a concise instruction like "Produce final summary and next-step recommendations based on gathered results." 
            
            ---
            
            Available actions:
            - **UPDATE_NETWORK** \u2192 when the network view needs to highlight or annotate specific nodes/edges (e.g., color seeds, pin top-ranked genes).
            - **FETCH_RESEARCH** \u2192 when background literature is required or the current answer needs more evidence.
            - **FETCH_KG** \u2192 when knowledge-graph context is needed. Prefer this if the question targets information typically present in a KG. NOT for DIAMOnD, TrustRank, closeness, or enrichment.
            - **FETCH_BIO_INFO** \u2192 when biological enrichment/augmentation of the query is needed.
            - **CALL_NEDREX_TOOL** \u2192 for DIAMOnD, TrustRank, closeness, or general drug repurposing. If seeds are already available and the user asked to run it, call this directly without fetching more context.
            - **CALL_DIGEST_TOOL** \u2192 when enrichment analysis is needed. If seeds are available and requested, call directly.
            - **FINALIZE** \u2192 when you can summarize and recommend next steps.
            
            ---
            
            Output policy:
            - Output ONLY a valid JSON object with fields: action, reason, subTaskQuestion.
            - No extra properties or text.
            
            ---
            ### Examples
            
            1) Need to update network with new seeds:
            {
              "action": "UPDATE_NETWORK",
              "reason": "User wants seed drugs highlighted in red",
              "subTaskQuestion": "Highlight provided seed drugs in red in the current network view."
            }
            
            2) Fetch research:
            {
              "action": "FETCH_RESEARCH",
              "reason": "User asked for supporting literature on TP53",
              "subTaskQuestion": "Retrieve recent papers linking TP53 to chemoresistance in triple-negative breast cancer."
            }
            
            3) Fetch KG context:
            {
              "action": "FETCH_KG",
              "reason": "Need NeDRex KG neighbors before algorithm run",
              "subTaskQuestion": "Get first- and second-degree neighbors of BRCA1 and BRCA2 from NeDRex KG."
            }
            
            4) Fetch biological info:
            {
              "action": "FETCH_BIO_INFO",
              "reason": "Query ambiguous, need enhanced bio info",
              "subTaskQuestion": "Normalize gene aliases for the user-provided seed list and return HGNC-approved symbols."
            }
            
            5) Call NeDRex tool:
            {
              "action": "CALL_NEDREX_TOOL",
              "reason": "DIAMOnD requested; seeds already provided",
              "subTaskQuestion": "Run DIAMOnD with the current seed genes (k=200) and return ranked candidates."
            }
            
            6) Call Digest tool:
            {
              "action": "CALL_DIGEST_TOOL",
              "reason": "Perform enrichment analysis on seed set",
              "subTaskQuestion": "Run GO Biological Process enrichment on the seed genes using DigestSet."
            }
            
            7) Finalize with recommendation:
            {
              "action": "FINALIZE",
              "reason": "All context gathered; providing summary",
              "subTaskQuestion": "Produce final summary and next-step recommendations based on gathered results."
            }
            
            ---
            """)
    @UserMessage("""
            # Planning context
            
            ## Current state
            {{state}}
            
            ## User goal
            Extract the overall intent from `state.userGoal` and use it to guide your decision.
            
            ## Previous decisions
            Do NOT repeat any of these actions. Each action may only be taken once.
            Never go back to a previous action.
            {#for h in history}
            - Action: {h.getAction()} | Reason: {h.getReason()}
            {/for}
            
            ## Steps left
            You have {stepsLeft} planning steps remaining.
            Minimize steps. Finalize early if possible. If you have 1 step left, you MUST finalize.
            
            ---
            ### Instructions
            - Choose **exactly one** next action.
            - Always output a single JSON object with fields:
              - `action`: the next PlanAction
              - `reason`: short, clear explanation
              - `subTaskQuestion`: a single, atomic, concise instruction for the next agent (≤ 25 words).
            - Never output extra text or properties.
            - If every required action is already in history, immediately return a `FINALIZE` step (with an appropriate `subTaskQuestion` to produce the final summary/recommendations).
            - If the user asked for NeDRex tool (DIAMOnD, TrustRank, closeness) and you have seeds, call it DIRECTLY without fetching KG information.
            - If the user asked for enrichment analysis and you have seeds, call it DIRECTLY without fetching KG information.
            
            Decide the next step now.
            """)
    PlanStep decide(PlanState state, List<PlanStep> history, int stepsLeft);
}