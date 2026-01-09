package de.hamburg.university.tool;


import com.fasterxml.jackson.core.type.TypeReference;
import de.hamburg.university.agent.bot.NeDRexBot;
import de.hamburg.university.agent.bot.NeDRexToolDecisionBot;
import de.hamburg.university.agent.tool.nedrex.NeDRexTool;
import de.hamburg.university.agent.tool.nedrex.NeDRexToolDecisionResult;
import de.hamburg.university.helper.AIJudgeBot;
import de.hamburg.university.helper.JsonLoader;
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
import java.util.List;

@QuarkusTest
public class NeDRexToolEvaluationTest {

    @Inject
    NeDRexTool neDRexTool;

    @Inject
    NeDRexBot neDRexBot;

    @Inject
    NeDRexToolDecisionBot neDRexToolDecisionBot;

    @Inject
    AIJudgeBot judgeBot;

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
    void agentDiamondTest() {
        List<NeDRexToolQuestion> questions = JsonLoader.loadJson("tools/nedrex/diamond/questions.json", new TypeReference<List<NeDRexToolQuestion>>() {
        });

        for (NeDRexToolQuestion question : questions) {
            NeDRexToolTestResult result = testDiamond(question);
            results.add(result);
            Log.info(result.toString());
            NeDRexToolTestResult.printJsonFile(results, getPath());
        }
    }

    @Test
    @Order(2)
    void agentTrustrankTest() {
        List<NeDRexToolQuestion> questions = JsonLoader.loadJson("tools/nedrex/trustrank/questions.json", new TypeReference<List<NeDRexToolQuestion>>() {
        });

        int trustrankResultCount = 0;
        for (NeDRexToolQuestion question : questions) {
            NeDRexToolTestResult result = testTrustrank(question);
            results.add(result);
            trustrankResultCount++;
            Log.info(result.toString());
            
            Path savePath = getPath();
            NeDRexToolTestResult.printJsonFile(results, savePath);
            Log.infof("Saved trustrank result for question: %s to path: %s (total results: %d)", 
                     question.getPath(), savePath.toAbsolutePath(), results.size());
        }
        Log.infof("Trustrank test completed. Added %d trustrank results. Total results in file: %d", 
                 trustrankResultCount, results.size());
    }

    @Test
    @Order(3)
    void agentClosenessTest() {
        List<NeDRexToolQuestion> questions = JsonLoader.loadJson("tools/nedrex/closeness/questions.json", new TypeReference<List<NeDRexToolQuestion>>() {
        });

        for (NeDRexToolQuestion question : questions) {
            NeDRexToolTestResult result = testCloseness(question);
            results.add(result);
            Log.info(result.toString());
            NeDRexToolTestResult.printJsonFile(results, getPath());
        }
    }

    private NeDRexToolTestResult testDiamond(NeDRexToolQuestion question) {
        NeDRexToolTestResult result = new NeDRexToolTestResult(question.getQuestion(), question.getPath());

        List<String> expectedSeeds = parseSteps(question.getSteps());
        if (expectedSeeds == null || expectedSeeds.isEmpty()) {
            Log.warnf("No steps found in question for path: %s", question.getPath());
            return result;
        }

        String enhancedContext = "";
        try {
            enhancedContext = neDRexBot.answer(question.getPath(), question.getQuestion(), "");
        } catch (Exception e) {
            Log.warnf(e, "Failed to get enhanced context from neDRexBot for question: %s. Using empty context.", question.getQuestion());
            enhancedContext = "";
        }
        NeDRexToolDecisionResult decision;
        try {
            decision = neDRexToolDecisionBot.answer("test-session", question.getQuestion(), enhancedContext);
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            String className = e.getClass().getSimpleName().toLowerCase();
            Throwable cause = e.getCause();
            String causeMsg = cause != null && cause.getMessage() != null ? cause.getMessage().toLowerCase() : "";
            String causeClass = cause != null ? cause.getClass().getSimpleName().toLowerCase() : "";
            
            boolean isParsingError = errorMsg.contains("parse") || errorMsg.contains("json") || 
                                    className.contains("parsing") || className.contains("output") ||
                                    causeMsg.contains("parse") || causeMsg.contains("json") ||
                                    causeClass.contains("parse") || causeClass.contains("json");
            
            boolean isHallucination = errorMsg.contains("hallucination") || errorMsg.contains("no such tool exists");
            
            if (isParsingError || isHallucination) {
                Log.warnf(e, "LLM returned invalid response for question: %s. Error type: %s. Returning empty result.", 
                         question.getQuestion(), isParsingError ? "parsing" : "hallucination");
                return result;
            }
            throw e;
        }
        if (decision.getEntrezIds() == null || decision.getEntrezIds().isEmpty()) {
            return result;
        }
        List<String> entrezIds = decision.getEntrezIds();
        checkInput(expectedSeeds, entrezIds, result);
        if (!result.isCorrectInput()) {
            return result;
        }
        if (!decision.getToolName().equalsIgnoreCase("diamond")) {
            return result;
        }
        result.setCorrectTool(true);

        result.setCorrectAnswer(true);
        return result;
    }

    private NeDRexToolTestResult testTrustrank(NeDRexToolQuestion question) {
        NeDRexToolTestResult result = new NeDRexToolTestResult(question.getQuestion(), question.getPath());

        List<String> expectedSeeds = parseSteps(question.getSteps());
        if (expectedSeeds == null || expectedSeeds.isEmpty()) {
            Log.warnf("No steps found in question for path: %s", question.getPath());
            return result;
        }

        String enhancedContext = "";
        try {
            enhancedContext = neDRexBot.answer(question.getPath(), question.getQuestion(), "");
        } catch (Exception e) {
            Log.warnf(e, "Failed to get enhanced context from neDRexBot for question: %s. Using empty context.", question.getQuestion());
            enhancedContext = "";
        }
        NeDRexToolDecisionResult decision;
        try {
            decision = neDRexToolDecisionBot.answer("test-session", question.getQuestion(), enhancedContext);
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            String className = e.getClass().getSimpleName().toLowerCase();
            Throwable cause = e.getCause();
            String causeMsg = cause != null && cause.getMessage() != null ? cause.getMessage().toLowerCase() : "";
            String causeClass = cause != null ? cause.getClass().getSimpleName().toLowerCase() : "";
            
            boolean isParsingError = errorMsg.contains("parse") || errorMsg.contains("json") || 
                                    className.contains("parsing") || className.contains("output") ||
                                    causeMsg.contains("parse") || causeMsg.contains("json") ||
                                    causeClass.contains("parse") || causeClass.contains("json");
            
            boolean isHallucination = errorMsg.contains("hallucination") || errorMsg.contains("no such tool exists");
            
            if (isParsingError || isHallucination) {
                Log.warnf(e, "LLM returned invalid response for question: %s. Error type: %s. Returning empty result.", 
                         question.getQuestion(), isParsingError ? "parsing" : "hallucination");
                return result;
            }
            throw e;
        }
        if (decision.getUniProtIds() == null || decision.getUniProtIds().isEmpty()) {
            return result;
        }
        List<String> uniProtIds = decision.getUniProtIds().stream()
                .map(id -> id != null && id.startsWith("uniprot.") ? id.replace("uniprot.", "") : id)
                .toList();
        checkInput(expectedSeeds, uniProtIds, result);
        if (!result.isCorrectInput()) {
            return result;
        }
        if (!decision.getToolName().equalsIgnoreCase("trustrank")) {
            return result;
        }
        result.setCorrectTool(true);

        result.setCorrectAnswer(true);
        return result;
    }

    private NeDRexToolTestResult testCloseness(NeDRexToolQuestion question) {
        NeDRexToolTestResult result = new NeDRexToolTestResult(question.getQuestion(), question.getPath());

        List<String> expectedSeeds = parseSteps(question.getSteps());
        if (expectedSeeds == null || expectedSeeds.isEmpty()) {
            Log.warnf("No steps found in question for path: %s", question.getPath());
            return result;
        }
        expectedSeeds = expectedSeeds.stream().distinct().toList();

        String enhancedContext = "";
        try {
            enhancedContext = neDRexBot.answer(question.getPath(), question.getQuestion(), "");
        } catch (Exception e) {
            Log.warnf(e, "Failed to get enhanced context from neDRexBot for question: %s. Using empty context.", question.getQuestion());
            enhancedContext = "";
        }
        NeDRexToolDecisionResult decision;
        try {
            decision = neDRexToolDecisionBot.answer("test-session", question.getQuestion(), enhancedContext);
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            String className = e.getClass().getSimpleName().toLowerCase();
            Throwable cause = e.getCause();
            String causeMsg = cause != null && cause.getMessage() != null ? cause.getMessage().toLowerCase() : "";
            String causeClass = cause != null ? cause.getClass().getSimpleName().toLowerCase() : "";
            
            boolean isParsingError = errorMsg.contains("parse") || errorMsg.contains("json") || 
                                    className.contains("parsing") || className.contains("output") ||
                                    causeMsg.contains("parse") || causeMsg.contains("json") ||
                                    causeClass.contains("parse") || causeClass.contains("json");
            
            boolean isHallucination = errorMsg.contains("hallucination") || errorMsg.contains("no such tool exists");
            
            if (isParsingError || isHallucination) {
                Log.warnf(e, "LLM returned invalid response for question: %s. Error type: %s. Returning empty result.", 
                         question.getQuestion(), isParsingError ? "parsing" : "hallucination");
                return result;
            }
            throw e;
        }
        if (decision.getUniProtIds() == null || decision.getUniProtIds().isEmpty()) {
            return result;
        }
        List<String> uniProtIds = decision.getUniProtIds().stream()
                .map(id -> id != null && id.startsWith("uniprot.") ? id.replace("uniprot.", "") : id)
                .distinct()
                .toList();
        checkInput(expectedSeeds, uniProtIds, result);
        if (!result.isCorrectInput()) {
            return result;
        }
        if (!decision.getToolName().equalsIgnoreCase("closeness")) {
            return result;
        }
        result.setCorrectTool(true);

        result.setCorrectAnswer(true);
        return result;
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


