package de.hamburg.university.tool;


import com.fasterxml.jackson.core.type.TypeReference;
import de.hamburg.university.agent.planning.bots.HelpBot;
import de.hamburg.university.helper.AIJudgeBot;
import de.hamburg.university.helper.JsonLoader;
import de.hamburg.university.helper.LoggingProgressBar;
import de.hamburg.university.tool.pojo.NeDRexToolQuestion;
import de.hamburg.university.tool.pojo.ResearchResult;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@QuarkusTest
public class HelpEvaluationTest {
    private static final Logger LOG = Logger.getLogger(HelpEvaluationTest.class);

    @Inject
    AIJudgeBot judgeBot;

    @Inject
    HelpBot help;

    @Test
    public void testHelpAiJudge() {

        List<NeDRexToolQuestion> questions = JsonLoader.loadJson("tools/help.json", new TypeReference<List<NeDRexToolQuestion>>() {
        });
        LoggingProgressBar bar = LoggingProgressBar.of(LOG, "Testing", questions.size());

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
            bar.step();
        }
        bar.complete();
        ResearchResult.printJsonFile(results, out);


        int correct = (int) results.stream().filter(ResearchResult::isCorrectAnswer).count();
        for(ResearchResult r : results) {
            LOG.info(r.minimizeString());
        }
        Log.info("_________________________________________");
        Log.infof("Correct answers: %d/%d", correct, results.size());
        Log.infof("JSON written: %s", out.toAbsolutePath());
    }

}
