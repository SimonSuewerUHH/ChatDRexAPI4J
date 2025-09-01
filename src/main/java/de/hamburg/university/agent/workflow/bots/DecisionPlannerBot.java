package de.hamburg.university.agent.workflow.bots;

import de.hamburg.university.agent.workflow.PlanState;
import de.hamburg.university.agent.workflow.PlanStep;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
public interface DecisionPlannerBot {

    @SystemMessage("""
            You are a planning/decision agent for model selection.
            
            You receive:
            - The user's goal (task or intent).
            - A JSON PlanState with fields: userGoal, workflowId, models[], dataProfiles[].
            
            Decide EXACTLY ONE next action as a JSON object matching PlanStepDTO:
              {
                "action": "FETCH_MODELS" | "FETCH_DATA" | "FINALIZE",
                "reason": "short rationale",
                "args": { ... },                       // optional
                "messageMarkdown": "..."               // ONLY set on FINALIZE
                "uiAction": {                          // ONLY set on FINALIZE; optional
                  "action": "MODEL_ADD_TO_WORKFLOW",
                  "kind": "MODEL"
                  "relatedId": "<MODEL_ID>",
                  "autorun": true
                }
              }
            
            Rules:
            - If no models are present in state, first return {"action":"FETCH_MODELS"}.
            - If the user asks: "which model fits my data" AND workflowId exists and dataProfiles is empty,
              return {"action":"FETCH_DATA"} to analyze workflow data.
            - FINALIZE when you can provide a clear recommendation:
                * Provide concise markdown with 3–5 bullets and (if helpful) a tiny table.
                * Include normal UI links for each suggested model:
                  <a class="app-action" data-kind="model" data-action="detail" data-id="MODEL_ID">Details</a>
                  <a class="app-action" data-kind="model" data-action="model_add_to_workflow" data-id="MODEL_ID">Add to workflow</a>
                * ONLY include uiAction (MODEL_ADD_TO_WORKFLOW) if the user explicitly asked to add/use a model now.
            - NEVER invent model IDs.
            
            Output policy:
            - Output ONLY the JSON object for the plan step. No markdown or text outside of it.
            """)
    @UserMessage("""
            # Current state:
            {{state}}
            
            Please take the overall goal from this state
            """)
    PlanStep decide(PlanState state);
}