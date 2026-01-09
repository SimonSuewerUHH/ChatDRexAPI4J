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
            chatWebsocketSender.clearTools();
            NeDRexToolTestResult result = testCloseness(question, true);
            results.add(result);
            NeDRexToolTestResult.printJsonFile(results, getPath());
        }
    }

    @Test
    @Order(2)
    void agentClosenessSetTest() {
        List<NeDRexToolQuestion> questions = JsonLoader.loadJson("tools/digest/set/questions.json", new TypeReference<List<NeDRexToolQuestion>>() {
        });

        for (NeDRexToolQuestion question : questions) {
            chatWebsocketSender.clearTools();
            NeDRexToolTestResult result = testCloseness(question, false);
            results.add(result);
            NeDRexToolTestResult.printJsonFile(results, getPath());
        }
    }

    private NeDRexToolTestResult testCloseness(NeDRexToolQuestion question, boolean subnetwork) {

        String folder = subnetwork ? "subnet" : "set";
        NeDRexToolTestResult result = new NeDRexToolTestResult(question.getQuestion(), question.getPath());
        String path = "tools/digest/" + folder + "/" + question.getPath();
        DigestResultResponseDTO resultMocked = JsonLoader.loadJson(path, new TypeReference<DigestResultResponseDTO>() {
        });

        DigestToolResultDTO mappedResult = null;
        try {
            mappedResult = digestFormatterService.formatDigestOutputStructured(resultMocked.getResult(), resultMocked.getTask());
        } catch (IllegalArgumentException e) {
            Log.warnf("Failed to format digest output (likely p-values don't meet cutoff) for question: %s. Error: %s", 
                     question.getPath(), e.getMessage());
        }

        String enhancedContext;
        String answer = null;
        try {
            enhancedContext = neDRexBot.answer(question.getPath(), question.getQuestion(), "");
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("exceeded") || msg.contains("Something is wrong")) && msg.contains("tool")) {
                Log.warnf("Tool execution limit exceeded for neDRexBot: %s", msg);
                enhancedContext = "";
            } else if (e instanceof NullPointerException || (msg != null && (msg.contains("null") || msg.contains("ChatCompletionResponse")))) {
                Log.warnf("OpenAI API returned null response in neDRexBot (continuing): %s", 
                         e.getClass().getSimpleName() + (msg != null ? ": " + msg : ""));
                enhancedContext = "";
            } else {
                Log.warnf("Exception in neDRexBot (continuing): %s", e.getClass().getSimpleName());
                enhancedContext = "";
            }
        }
        
        try {
            answer = digestBot.answer(question.getPath(), question.getQuestion(), enhancedContext);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            String botName = "digestBot";
            if (msg != null && (msg.contains("exceeded") || msg.contains("Something is wrong")) && msg.contains("tool")) {
                Log.warnf("Tool execution limit exceeded for %s: %s", botName, msg);
            } else if (msg != null && (msg.contains("getUniProtIds") || msg.contains("hallucination") || msg.contains("no such tool"))) {
                Log.warnf("LLM tried to call non-existent tool in %s: %s", botName, msg);
            } else if (e instanceof NullPointerException || (msg != null && (msg.contains("null") || msg.contains("ChatCompletionResponse")))) {
                Log.warnf("OpenAI API error in %s (continuing): %s", botName, e.getClass().getSimpleName());
            } else {
                Log.warnf("Exception in %s (continuing): %s", botName, e.getClass().getSimpleName());
            }
        }

        List<ToolDTO> digestTools = chatWebsocketSender.findToolByToolName(Tools.DIGEST);
        List<String> input = digestTools.stream()
                .filter(t -> t.getInput() != null)  
                .findFirst()
                .map(t -> {
                    Object inputObj = t.getInput();
                    return Arrays.stream(inputObj.toString().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
                })
                .orElse(new ArrayList<>());

        if (!input.isEmpty()) {
            result.setCorrectTool(true);
        } else {
            result.setCorrectTool(false);
        }

        List<String> expectedTarget = parseSteps(question.getSteps());
        checkInput(input, expectedTarget, result);

        if (answer != null && !answer.trim().isEmpty() && mappedResult != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String mockedJson = mapper.writeValueAsString(mappedResult);
                
                boolean correctAnswer = judgeBot.isAnswerCorrectGivenContext(
                        question.getQuestion(),
                        answer,
                        mockedJson,
                        "No additional rules."
                );
                result.setCorrectAnswer(correctAnswer);
            } catch (JsonProcessingException e) {
                Log.errorf(e, "Failed to serialize mocked result for question: %s", question.getPath());
            } catch (Exception e) {
                Log.errorf(e, "Failed to evaluate answer for question: %s", question.getPath());
            }
        } else {
            if (answer == null || answer.trim().isEmpty()) {
                Log.warnf("No answer to evaluate for question: %s", question.getPath());
            }
            if (mappedResult == null) {
                Log.warnf("No mocked result available for question: %s (p-values may not meet cutoff)", question.getPath());
            }
        }
        
        return result;
    }

    private void checkInput(List<String> list1, List<String> list2, NeDRexToolTestResult result) {
        boolean correctInput = true;
        if (list1 == null || list2 == null) {
            result.setCorrectInput(false);
            Log.warnf("Input check failed: null values - list1=%s, list2=%s", list1, list2);
            return;
        }
        
        List<String> normalizedList1 = list1.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        List<String> normalizedList2 = list2.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        
        if (normalizedList1.size() != normalizedList2.size()) {
            correctInput = false;
            Log.warnf("Input size mismatch for extracted=%s vs expected=%s", normalizedList1, normalizedList2);
        }
        
        for (String item : normalizedList2) {
            String trimmedItem = item.trim();
            if (!normalizedList1.contains(trimmedItem)) {
                result.addMissingInput(trimmedItem);
                correctInput = false;
            }
        }
        
        for (String item : normalizedList1) {
            if (!normalizedList2.contains(item)) {
                Log.warnf("Extra item in extracted input: %s (not in expected: %s)", item, normalizedList2);
            }
        }

        result.setCorrectInput(correctInput);
    }

    private List<String> parseSteps(String steps) {
        if (steps == null || steps.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        String[] parts = steps.split("[, ]+");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    static Path getPath() {
        return Paths.get("results", "eval", modelName.replace(":latest", ""), "digest.json");
    }
}


