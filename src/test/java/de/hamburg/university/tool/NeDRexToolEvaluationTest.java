package de.hamburg.university.tool;


import com.fasterxml.jackson.core.type.TypeReference;
import de.hamburg.university.agent.bot.NeDRexBot;
import de.hamburg.university.agent.bot.NeDRexToolDecisionBot;
import de.hamburg.university.agent.tool.nedrex.NeDRexTool;
import de.hamburg.university.agent.tool.nedrex.NeDRexToolDecisionResult;
import de.hamburg.university.helper.AIJudgeBot;
import de.hamburg.university.helper.JsonLoader;
import de.hamburg.university.helper.drugstone.DrugstOneGraphHelper;
import de.hamburg.university.helper.drugstone.dto.DrugstOneNetworkDTO;
import de.hamburg.university.service.nedrex.closeness.ClosenessResultDTO;
import de.hamburg.university.service.nedrex.diamond.DiamondResultsDTO;
import de.hamburg.university.service.nedrex.trustrank.TrustRankResultDTO;
import de.hamburg.university.tool.pojo.NeDRexToolQuestion;
import de.hamburg.university.tool.pojo.NeDRexToolTestResult;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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
    DrugstOneGraphHelper drugstOneGraphHelper;

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

        for (NeDRexToolQuestion question : questions) {
            NeDRexToolTestResult result = testTrustrank(question);
            results.add(result);
            Log.info(result.toString());
            NeDRexToolTestResult.printJsonFile(results, getPath());
        }
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
        String path = "tools/nedrex/diamond/" + question.getPath();
        DiamondResultsDTO resultMocked = JsonLoader.loadJson(path, new TypeReference<DiamondResultsDTO>() {
        });


        String enhancedContext = neDRexBot.answer(question.getPath(), question.getQuestion(), "");
        NeDRexToolDecisionResult decision = neDRexToolDecisionBot.answer(question.getQuestion(), enhancedContext);
        if (decision.getEntrezIds() == null || decision.getEntrezIds().isEmpty()) {
            return result;
        }
        List<String> entrezIds = decision.getEntrezIds();
        checkInput(resultMocked.getSeeds(), entrezIds, result);
        if (!result.isCorrectInput()) {
            return result;
        }
        if (!decision.getToolName().equalsIgnoreCase("diamond")) {
            return result;
        }
        result.setCorrectTool(true);

        //As we now that the input correct, we can assume that the output is correct if the tool is called
        DrugstOneNetworkDTO network = drugstOneGraphHelper.diamondToNetwork(resultMocked);
        if (network.getNodes().isEmpty()) {
            return result;
        }
        result.setCorrectAnswer(true);
        return result;
    }

    private NeDRexToolTestResult testTrustrank(NeDRexToolQuestion question) {
        NeDRexToolTestResult result = new NeDRexToolTestResult(question.getQuestion(), question.getPath());
        String path = "tools/nedrex/trustrank/" + question.getPath();
        TrustRankResultDTO resultMocked = JsonLoader.loadJson(path, new TypeReference<TrustRankResultDTO>() {
        });


        String enhancedContext = neDRexBot.answer(question.getPath(), question.getQuestion(), "");
        NeDRexToolDecisionResult decision = neDRexToolDecisionBot.answer(question.getQuestion(), enhancedContext);
        if (decision.getEntrezIds() == null || decision.getEntrezIds().isEmpty()) {
            return result;
        }
        List<String> entrezIds = decision.getEntrezIds();
        checkInput(resultMocked.getSeedProteins(), entrezIds, result);
        if (!result.isCorrectInput()) {
            return result;
        }
        if (!decision.getToolName().equalsIgnoreCase("trustrank")) {
            return result;
        }
        result.setCorrectTool(true);

        //As we now that the input correct, we can assume that the output is correct if the tool is called
        DrugstOneNetworkDTO network = drugstOneGraphHelper.trustrankToNetwork(resultMocked);
        if (network.getNodes().isEmpty()) {
            return result;
        }
        result.setCorrectAnswer(true);
        return result;
    }

    private NeDRexToolTestResult testCloseness(NeDRexToolQuestion question) {
        NeDRexToolTestResult result = new NeDRexToolTestResult(question.getQuestion(), question.getPath());
        String path = "tools/nedrex/closeness/" + question.getPath();
        ClosenessResultDTO resultMocked = JsonLoader.loadJson(path, new TypeReference<ClosenessResultDTO>() {
        });


        String enhancedContext = neDRexBot.answer(question.getPath(), question.getQuestion(), "");
        NeDRexToolDecisionResult decision = neDRexToolDecisionBot.answer(question.getQuestion(), enhancedContext);
        if (decision.getEntrezIds() == null || decision.getEntrezIds().isEmpty()) {
            return result;
        }
        List<String> entrezIds = decision.getEntrezIds();
        checkInput(resultMocked.getSeedProteins(), entrezIds, result);
        if (!result.isCorrectInput()) {
            return result;
        }
        if (!decision.getToolName().equalsIgnoreCase("closeness")) {
            return result;
        }
        result.setCorrectTool(true);

        //As we now that the input correct, we can assume that the output is correct if the tool is called
        DrugstOneNetworkDTO network = drugstOneGraphHelper.trustrankToNetwork(resultMocked);
        if (network.getNodes().isEmpty()) {
            return result;
        }
        result.setCorrectAnswer(true);
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


