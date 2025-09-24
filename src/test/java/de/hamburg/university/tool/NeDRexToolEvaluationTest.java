package de.hamburg.university.tool;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.agent.bot.NeDRexBot;
import de.hamburg.university.agent.bot.NeDRexToolDecisionBot;
import de.hamburg.university.agent.tool.nedrex.NeDRexTool;
import de.hamburg.university.agent.tool.nedrex.NeDRexToolDecisionResult;
import de.hamburg.university.helper.AIJudgeBot;
import de.hamburg.university.helper.JsonLoader;
import de.hamburg.university.helper.drugstone.DrugstOneGraphHelper;
import de.hamburg.university.helper.drugstone.DrugstOneNetworkDTO;
import de.hamburg.university.service.nedrex.diamond.DiamondResultsDTO;
import de.hamburg.university.tool.pojo.NeDRexToolQuestion;
import de.hamburg.university.tool.pojo.NeDRexToolTestResult;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

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

    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();


    @Test
    void agentDiamondTest() {
        List<NeDRexToolQuestion> questions = JsonLoader.loadJson("tools/nedrex/diamond/questions.json", new TypeReference<List<NeDRexToolQuestion>>() {
        });

        List<NeDRexToolTestResult> results = new ArrayList<>();
        for (NeDRexToolQuestion question : questions) {
            NeDRexToolTestResult result = testDiamond(question);
            results.add(result);
            Log.info(result.toString());
        }
    }

    private NeDRexToolTestResult testDiamond(NeDRexToolQuestion question) {
        NeDRexToolTestResult result = new NeDRexToolTestResult(question.getQuestion(), question.getPath());
        String path = "tools/nedrex/diamond/" + question.getPath();
        DiamondResultsDTO resultMocked = JsonLoader.loadJson(path, new TypeReference<DiamondResultsDTO>() {
        });


        String enhancedContext = neDRexBot.answer(question.getPath(), question.getQuestion(), "");
        NeDRexToolDecisionResult decision = neDRexToolDecisionBot.answer(question.getQuestion(), enhancedContext);
        if (decision.getEntrezIds().isEmpty()) {
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

    private void checkInput(List<String> list1, List<String> list2, NeDRexToolTestResult result) {
        boolean correctInput = true;
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
}


