package de.hamburg.university.tool;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.agent.bot.ResearchBot;
import de.hamburg.university.agent.planning.bots.HelpBot;
import de.hamburg.university.agent.tool.ToolStructuredContentType;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.agent.tool.research.ToolSourceDTO;
import de.hamburg.university.helper.AIJudgeBot;
import de.hamburg.university.helper.JsonLoader;
import de.hamburg.university.socket.TestChatWebsocketSender;
import de.hamburg.university.tool.pojo.NeDRexToolQuestion;
import de.hamburg.university.tool.pojo.ResearchResult;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@QuarkusTest
public class HelpEvaluationTest {

    @Inject
    AIJudgeBot judgeBot;

    @Inject
    HelpBot help;

    @Test
    public void testInteractions() {
        List<NeDRexToolQuestion> questions = JsonLoader.loadJson("tools/help.json", new TypeReference<List<NeDRexToolQuestion>>() {
        });

        List<ResearchResult> results = new ArrayList<>();
        Path out = Paths.get("results", "eval", "help.json");

        for (NeDRexToolQuestion question : questions) {
            ResearchResult result = new ResearchResult();
            result.setQuestion(question.getQuestion());

            try {
                String answer = help.answer(question.getQuestion());

                result.setAnswer(answer);

                boolean correctAnswer = judgeBot.isAnswerCorrectGivenContext(
                        question.getQuestion(),
                        answer,
                        "No context.",
                        "No additional rules."
                );

                result.setCorrectAnswer(correctAnswer);

            } catch (Exception e) {
                result.setErrorMessage(e.getMessage());
            }
            results.add(result);
            Log.info(result);

        }

        ResearchResult.printJsonFile(results, out);

    }

}
