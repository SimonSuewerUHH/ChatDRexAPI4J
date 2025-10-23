package de.hamburg.university.tool;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.agent.bot.DIGESTBot;
import de.hamburg.university.agent.bot.NeDRexBot;
import de.hamburg.university.agent.tool.ToolDTO;
import de.hamburg.university.agent.tool.Tools;
import de.hamburg.university.helper.AIJudgeBot;
import de.hamburg.university.helper.JsonLoader;
import de.hamburg.university.service.digest.DigestFormatterService;
import de.hamburg.university.service.digest.DigestResultResponseDTO;
import de.hamburg.university.service.digest.DigestToolResultDTO;
import de.hamburg.university.socket.TestChatWebsocketSender;
import de.hamburg.university.tool.pojo.NeDRexToolQuestion;
import de.hamburg.university.tool.pojo.NeDRexToolTestResult;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@QuarkusTest
public class DigestEvaluationTest {

    @Inject
    DIGESTBot digestBot;

    @Inject
    AIJudgeBot judgeBot;

    @Inject
    TestChatWebsocketSender chatWebsocketSender;

    @Inject
    NeDRexBot neDRexBot;

    @Inject
    DigestFormatterService digestFormatterService;

    static List<NeDRexToolTestResult> results = new ArrayList<>();

    private static final boolean REPLACE_MODE = false;
    private static String modelName = ConfigProvider.getConfig().getValue("quarkus.langchain4j.openai.chat-model.model-name", String.class);


    @BeforeAll
    static void testSetup() {
        if (REPLACE_MODE) {
            Log.warn("REPLACE MODE is ON - existing results will be overwritten!");
        } else {
            Log.info("REPLACE MODE is OFF - existing results will be kept!");
            results = NeDRexToolTestResult.loadJsonFile(getPath());
            if (results == null) {
                results = new ArrayList<>();
                Log.info("No question results found!");
            }
        }
    }

    @Test
    @Order(1)
    void agentClosenessSubnetTest() {
        List<NeDRexToolQuestion> questions = JsonLoader.loadJson("tools/digest/subnet/questions.json", new TypeReference<List<NeDRexToolQuestion>>() {
        });

        for (NeDRexToolQuestion question : questions) {
            NeDRexToolTestResult result = testCloseness(question, true);
            results.add(result);
            Log.info(result.toString());
            NeDRexToolTestResult.printJsonFile(results, getPath());
        }
    }

    @Test
    @Order(2)
    void agentClosenessSetTest() {
        List<NeDRexToolQuestion> questions = JsonLoader.loadJson("tools/digest/set/questions.json", new TypeReference<List<NeDRexToolQuestion>>() {
        });

        for (NeDRexToolQuestion question : questions) {
            NeDRexToolTestResult result = testCloseness(question, false);
            results.add(result);
            Log.info(result.toString());
            NeDRexToolTestResult.printJsonFile(results, getPath());
        }
    }

    private NeDRexToolTestResult testCloseness(NeDRexToolQuestion question, boolean subnetwork) {

        String folder = subnetwork ? "subnet" : "set";
        String toolContentContains = subnetwork ? "DIGEST-Subnetwork" : "DIGEST-Set";
        NeDRexToolTestResult result = new NeDRexToolTestResult(question.getQuestion(), question.getPath());
        String path = "tools/digest/" + folder + "/" + question.getPath();
        DigestResultResponseDTO resultMocked = JsonLoader.loadJson(path, new TypeReference<DigestResultResponseDTO>() {
        });

        DigestToolResultDTO mappedResult = digestFormatterService.formatDigestOutputStructured(resultMocked.getResult(), resultMocked.getTask());

        String enhancedContext = neDRexBot.answer(question.getPath(), question.getQuestion(), "");
        String answer = digestBot.answer(question.getPath(), question.getQuestion(), enhancedContext);

        List<ToolDTO> context = chatWebsocketSender.findToolByToolName(Tools.DIGEST);
        List<String> input = context.stream()
                .filter(t -> !t.getContent().stream().filter(c -> c.contains(toolContentContains)).toList().isEmpty())
                .findFirst()
                .map(t -> Arrays.asList(t.getInput().toString().split(", ")))
                .orElse(new ArrayList<>());

        checkInput(input, resultMocked.getParameters().getTarget(), result);
        if (!result.isCorrectInput()) {
            return result;
        }
        if (input.isEmpty()) {
            return result;
        }
        result.setCorrectTool(true);

        ObjectMapper mapper = new ObjectMapper();
        String mockedJson = mappedResult.toString();
        try {
            mockedJson = mapper.writeValueAsString(mappedResult);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        boolean correctAnswer = judgeBot.isAnswerCorrectGivenContext(
                question.getQuestion(),
                answer,
                mockedJson,
                "No additional rules."
        );

        result.setCorrectAnswer(correctAnswer);
        return result;
    }

    private void checkInput(List<String> list1, List<String> list2, NeDRexToolTestResult result) {
        boolean correctInput = true;
        if (list1 == null || list2 == null) {
            result.setCorrectInput(false);
            return;
        }
        if (list1.size() != list2.size()) {
            correctInput = false;
        }
        for (String item : list1) {
            if (!list2.contains(item)) {
                result.addMissingInput(item);
                correctInput = false;
            }
        }

        result.setCorrectInput(correctInput);
    }

    static Path getPath() {
        return Paths.get("results", "eval", modelName.replace(":latest", ""), "digest.json");
    }
}


