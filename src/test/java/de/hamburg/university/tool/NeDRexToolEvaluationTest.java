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
import de.hamburg.university.service.nedrex.diamond.SeedPayloadDTO;
import de.hamburg.university.tool.pojo.NeDRexToolQuestion;
import de.hamburg.university.tool.pojo.NeDRexToolTestResult;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

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
        NeDRexToolQuestion test = new NeDRexToolQuestion();
        test.setQuestion("Run the DIAMOnD algorithm, using the seed genes PDCD1, CD274, LAG3, and HAVCR2, to perform iterative module expansion based on connectivity significance. Return the ranked list of neighbor genes generated in this expansion, mapped to their corresponding Entrez Gene IDs.");
        test.setPath("001.json");
        NeDRexToolTestResult result = testDiamond(test);
        System.out.println("Tested question: " + test.getQuestion());
        System.out.println("Correct Input: " + result.isCorrectInput());
        System.out.println("Correct Tool: " + result.isCorrectTool());
        System.out.println("Correct Answer: " + result.isCorrectAnswer());
        System.out.println("--------------------------------------------------");
    }

    private NeDRexToolTestResult testDiamond(NeDRexToolQuestion question) {
        NeDRexToolTestResult result = new NeDRexToolTestResult();
        NeDRexTool spy = Mockito.spy(neDRexTool);
        String path = "tools/nedrex/diamond/" + question.getPath();
        DiamondResultsDTO resultMocked = JsonLoader.loadJson(path, new TypeReference<DiamondResultsDTO>() {
        });
        doReturn(Uni.createFrom().item(new DiamondResultsDTO()))
                .when(spy).runDiamond(any());


        String enhancedContext = neDRexBot.answer(question.getPath(), question.getQuestion(), "");
        NeDRexToolDecisionResult decision = neDRexToolDecisionBot.answer(question.getQuestion(), enhancedContext);
        if (decision.getEntrezIds().isEmpty()) {
            return result;
        }
        List<String> entrezIds = decision.getEntrezIds();
        result.setCorrectInput(checkLists(resultMocked.getSeeds(), entrezIds));
        if (!result.isCorrectInput()) {
            return result;
        }
        if (!decision.getToolName().equalsIgnoreCase("diamond")) {
            return result;
        }
        result.setCorrectTool(true);
        SeedPayloadDTO payload = neDRexTool.getDiamondPayload(entrezIds);

        Uni<DiamondResultsDTO> diamondResult = spy.runDiamond(payload);
        DrugstOneNetworkDTO network = drugstOneGraphHelper.diamondToNetwork(diamondResult.await().indefinitely());
        if (network.getNodes().isEmpty()) {
            return result;
        }
        result.setCorrectAnswer(true);
        return result;
    }

    private boolean checkLists(List<String> list1, List<String> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (String item : list1) {
            if (!list2.contains(item)) {
                return false;
            }
        }
        return true;
    }
}


