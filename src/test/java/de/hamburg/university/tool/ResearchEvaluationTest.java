package de.hamburg.university.tool;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.agent.bot.ResearchBot;
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
public class ResearchEvaluationTest {

    @Inject
    AIJudgeBot judgeBot;

    @Inject
    TestChatWebsocketSender chatWebsocketSender;

    @Inject
    ResearchBot research;


    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    public void testInteractions() {
        List<NeDRexToolQuestion> questions = JsonLoader.loadJson("tools/research/questions.json", new TypeReference<List<NeDRexToolQuestion>>() {
        });

        List<ResearchResult> results = new ArrayList<>();
        Path out = Paths.get("results", "eval", "research.csv");

        for (NeDRexToolQuestion question : questions) {
            ResearchResult result = new ResearchResult();
            result.setQuestion(question.getQuestion());

            String id = UUID.randomUUID().toString();
            String answer = research.answer(id, question.getQuestion(), "");
            List<ToolSourceDTO> sources = chatWebsocketSender.findContentByToolAndContentType(Tools.RESEARCH, ToolStructuredContentType.SOURCE);

            result.setSearchResults(sources);
            result.setAnswer(answer);

            boolean correctAnswer = judgeBot.isAnswerCorrectGivenContext(
                    question.getQuestion(),
                    answer,
                    papersToString(sources),
                    "No additional rules."
            );

            result.setCorrectAnswer(correctAnswer);

            Log.info(result);
            results.add(result);

        }

        ResearchResult.printJsonFile(results, out);

    }


    private String papersToString(List<ToolSourceDTO> papers) {
        StringBuilder sb = new StringBuilder();
        for (var paper : papers) {
            sb.append("Paper ID: ").append(paper.getPaperId()).append("\n");
            sb.append("Title: ").append(paper.getTitle()).append("\n");
            if (paper.getPublishedYear() != null) {
                sb.append("Year: ").append(paper.getPublishedYear()).append("\n");
            }
            if (paper.getAbstractText() != null) {
                sb.append("Abstract: ").append(paper.getAbstractText()).append("\n");
            }
            sb.append("-----\n");
        }
        return sb.toString();
    }


}
