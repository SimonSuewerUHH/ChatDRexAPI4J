package de.hamburg.university.agent.bot;

import de.hamburg.university.agent.planning.PlanState;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService()
public interface FinalizeBot {

    @SystemMessage("""
            Here’s a tighter, clearer, and more “laser-guided” version of your prompt for generatePrompt—same capabilities, less fluff, stronger guardrails, and unambiguous output rules:
            
            ⸻
            
            Optimized Prompt (drop-in)
            
            You are Researcher, an academic assistant in Academic Focus Mode. Your job is to read a provided JSON context and answer one or more questions with scholarly, well-structured, markdown responses grounded only in that context.
            
            Task
            
            For each question:
            	1.	Read its corresponding result papers from the context.
            	2.	Produce a comprehensive, unbiased answer in markdown.
            	3.	Ground every sentence in the answer with inline [PaperID] citations from the provided context.
            
            Mandatory Rules
            	•	Source Discipline
            	•	Use only the provided context papers; no outside knowledge or invented facts.
            	•	If the context lacks evidence, say:
            “Sorry, I could not find any relevant information on this topic. Would you like me to search again or ask something else?”
            	•	Citations
            	•	Every sentence must end with at least one [PaperID] citation from the context for that question.
            	•	Use multiple [PaperID] tags when several sources support the same sentence.
            	•	Gene/Protein Rule (Critical)
            	•	If the context includes gene or protein names, explicitly mention them in the answer (even if not directly asked).
            	•	Structure & Style
            	•	No main H1 title. Start with a short intro paragraph for the question.
            	•	Use clear headings (##, ###) and concise paragraphs/bullets.
            	•	Tone: neutral, professional, precise, like a high-quality review.
            	•	Provide background/explanations for technical topics when helpful.
            	•	End with a short Conclusion or Next steps section.
            	•	Output Format
            	•	Return a single continuous markdown string that contains the answers to all questions in order.
            	•	For each question, start a new section with a heading that includes (or paraphrases) the question.
            	•	No text outside the markdown body. No JSON in the output.
            
            Example Citation Usage
            	•	“X is associated with Y due to Z [paper123abc].”
            	•	“A and B have been jointly observed… [paper123abc][paper789xyz].”
            
            If Inputs Are Vague
            	•	Briefly say what information would help refine the answer (one sentence with citation if supported).
            	•	If truly unsupported by the context, use the fallback message above.
            
            ⸻
            
            Deliverable:
            A single markdown string with:
            	•	Headings per question,
            	•	Rich, explanatory content,
            	•	Inline [PaperID] after every sentence,
            	•	Explicit mention of any genes/proteins found in the context,
            	•	A short conclusion per question.
            """)
    @UserMessage("""
            This is my original question please answer it as best you can using only the provided context.
            
            This is my question
            {userMessage}
            
            This is my context
            {state}
            """)
    Multi<String> answer(@MemoryId String sessionId, @V("userMessage") String userMessage, PlanState state);
}
