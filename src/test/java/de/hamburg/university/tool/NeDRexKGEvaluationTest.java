package de.hamburg.university.tool;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.ChatdrexConfig;
import de.hamburg.university.agent.bot.kg.NeDRexKGBot;
import de.hamburg.university.agent.bot.kg.NeDRexKGGraph;
import de.hamburg.university.agent.tool.nedrex.kg.NeDRexKGTool;
import de.hamburg.university.helper.AIJudgeBot;
import de.hamburg.university.helper.JsonLoader;
import de.hamburg.university.service.nedrex.NeDRexApiClient;
import de.hamburg.university.service.nedrex.kg.NeDRexKGNodeEnhanced;
import de.hamburg.university.service.nedrex.kg.NeDRexKgQueryServiceImpl;
import de.hamburg.university.tool.helper.NeDRexKGEvaluationHelper;
import de.hamburg.university.tool.pojo.*;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.*;

@QuarkusTest
public class NeDRexKGEvaluationTest {
    @Inject
    NeDRexKgQueryServiceImpl nedrexKgQueryService;

    @Inject
    NeDRexKGBot nedrexKGBot;

    @Inject
    ChatdrexConfig config;

    @Inject
    NeDRexKGTool neDRexKGTool;

    @Inject
    @RestClient
    NeDRexApiClient neDRexApiClient;

    @Inject
    AIJudgeBot judgeBot;

    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private static final boolean REPLACE_MODE = true;
    @ConfigProperty(name = "quarkus.langchain4j.openai.chat-model.model-name", defaultValue = "default")
    String modelName;

    @Test
    public void testInteractions() {
        List<String> categories = List.of(
                "Drug",
                "Disorder",
                "GenomicVariant",
                "Phenotype",
                "Signature",
                "GO",
                "Protein",
                "Tissue",
                "Gene",
                "Pathway",
                "SideEffect"
        );
        List<Score> allScores = new ArrayList<>();
        List<QuestionScore> allQuestionScores = new ArrayList<>();
        Path out = Paths.get("results", "eval", modelName.replace(":latest", ""), "kg_cypher_result.csv");

        if (REPLACE_MODE) {
            Log.warn("REPLACE MODE is ON - existing results will be overwritten!");
        } else {
            Log.info("REPLACE MODE is OFF - existing results will be kept!");
            allQuestionScores = QuestionScore.loadJsonFile(out.resolveSibling("interactions_scores.json"));
            if (allQuestionScores == null) {
                allQuestionScores = new ArrayList<>();
                Log.info("No question scores found!");
            }
        }
        for (String category : categories) {
            List<CypherQuestion> questions = loadQuestions(category);
            List<Score> categoryScores = new ArrayList<>();
            for (CypherQuestion question : questions) {
                if (!REPLACE_MODE && QuestionScore.containsQuestion(allQuestionScores, question.getNlQuestion())) {
                    Log.info("Skipping already evaluated question: " + question.getNlQuestion());
                    continue;
                }
                try {

                    Log.info("Question " + (questions.indexOf(question) + 1) + "/" + questions.size() + " [" + (categories.indexOf(category) + 1) + "/" + categories.size() + "]: " + question.getNlQuestion());
                    List<Map<String, String>> result = query(question.getCypherTranslation());
                    AiCypher answer = fireAICypher(question.getNlQuestion());
                    Score score = NeDRexKGEvaluationHelper.score(result, answer.getResults());
                    Log.info("Length Golden: " + result.size() + ", LLM: " + answer.getResults().size() + ", Score: " + score);
                    Log.info("--------------------------------------------------");
                    categoryScores.add(score);
                    allScores.add(score);
                    allQuestionScores.add(new QuestionScore(category, question.getNlQuestion(), question.getCypherTranslation(), answer, score));

                    if (score.getPrecision() < 0.5) {
                        Log.warnf("Low precision for question: %s\nCypher: %s\nGolden: %s\nAI: %s\nScore: %s",
                                question.getNlQuestion(), question.getCypherTranslation(), result, answer, score);
                    }
                } catch (Exception e) {
                    Log.errorf(e, "Failed to validate question: %s", question.getNlQuestion());
                }
            }
            Score avgCategoryScore = Score.average(categoryScores);
            Log.infof("== Category " + category + " Avg Score ==" + avgCategoryScore);
            if (avgCategoryScore.getPrecision() < 0.5) {
                Log.warnf("Low average precision for category: %s, Score: %s", category, avgCategoryScore);
            }
            QuestionScore.printCsvFile(allQuestionScores, out);
            QuestionScore.printJsonFile(allQuestionScores, out.resolveSibling("interactions_scores.json"));

            /*assertTrue(avgCategoryScore.getPrecision() > 0.5,
                    "Category " + category + " precision avg should be > 0.5 but was " + avgCategoryScore.getPrecision());*/

        }

        Score overall = Score.average(allScores);
        Log.info("==== Overall Score ====");
        Log.info(overall);
       /* assertTrue(overall.getPrecision() > 0.5,
                "Overall precision should be > 0.5 but was " + overall.getPrecision());*/

    }

    @Test
    public void testAnswer() {
        List<String> categories = List.of(
                "Drug",
                "Disorder",
                "GenomicVariant",
                "Phenotype",
                "Signature",
                "GO",
                "Protein",
                "Tissue",
                "Gene",
                "Pathway",
                "SideEffect"
        );
        int total = 0;
        int correct = 0;
        List<AiAnswerCypher> allQuestionScores = new ArrayList<>();

        Path out = Paths.get("results", "eval", modelName.replace(":latest", ""), "kg_cypher_result_ai_judge.csv");

        for (String category : categories) {
            List<CypherQuestion> questions = loadQuestions(category);
            for (CypherQuestion question : questions) {
                try {
                    Log.info("Question " + (questions.indexOf(question) + 1) + "/" + questions.size() + " [" + (categories.indexOf(category) + 1) + "/" + categories.size() + "]: " + question.getNlQuestion());
                    AiAnswerCypher answer = answerTest(question.getNlQuestion());
                    boolean result = judgeBot.isAnswerCorrectGivenContext(
                            question.getNlQuestion(),
                            answer.getAnswer(),
                            answer.getContext(),
                            "No additional rules."
                    );
                    answer.setIsCorrect(result);
                    answer.setQuestion(question.getNlQuestion());
                    allQuestionScores.add(answer);
                    total++;
                    if (result) correct++;
                    Log.info("Judge result: " + result);
                } catch (Exception e) {
                    Log.errorf(e, "Failed to validate question: %s", question.getNlQuestion());
                }
            }
            float currentCorrect = correct / (float) total;
            Log.infof("== Category " + category + " Accuracy: " + currentCorrect + " (" + correct + "/" + total + ")");

            AiAnswerCypher.printJsonFile(allQuestionScores, out);

            /*assertTrue(avgCategoryScore.getPrecision() > 0.5,
                    "Category " + category + " precision avg should be > 0.5 but was " + avgCategoryScore.getPrecision());*/

        }
        float currentCorrect = correct / (float) total;

        Log.info("==== Overall Score ====");
        Log.info(currentCorrect);
       /* assertTrue(overall.getPrecision() > 0.5,
                "Overall precision should be > 0.5 but was " + overall.getPrecision());*/


    }


    private AiAnswerCypher answerTest(String question) {
        double minScore = config.tools().kgQuery().minGeneDisorderScore();
        NeDRexKGGraph questionGraph = neDRexKGTool.decomposeToNodes(question, "");
        List<NeDRexKGNodeEnhanced> enhancedNodes = nedrexKgQueryService.enhanceGraph(questionGraph);
        String enhancedNodesString = neDRexKGTool.stringifyEnhancedNodesToHTML(enhancedNodes);
        String oldQuery = "";
        String newQuery = "";
        final int maxAttempts = config.tools().kgQuery().retries();
        for (int i = 0; i < maxAttempts; i++) {
            try {
                newQuery = nedrexKGBot.generateCypherQuery(question, enhancedNodesString, oldQuery, minScore);
                oldQuery += "\n " + i + ". " + newQuery;
                String result = nedrexKgQueryService.fireNeo4jQuery(newQuery);
                String answer = nedrexKGBot.answerQuestion(question, result);
                return new AiAnswerCypher(answer, result, newQuery, i);
            } catch (ClientWebApplicationException e) {
                Log.errorf("Failed to query: %s (%s)", newQuery, e.getMessage());
            } catch (Exception e) {
                Log.warnf(e, "Attempt %d: Failed to generate answer for question: %s", i + 1, question);
            }

        }
        List<NeDRexKGNodeEnhanced> enhancedNodesFallback = nedrexKgQueryService.enhanceFallbackNodes(enhancedNodes);
        String enhancedNodesFallbackString = neDRexKGTool.stringifyEnhancedNodes(enhancedNodesFallback);
        String answer = nedrexKGBot.answerFallbackQuestion(question, enhancedNodesFallbackString);
        return new AiAnswerCypher(answer, enhancedNodesFallbackString);
    }

    private AiCypher fireAICypher(String question) {
        double minScore = config.tools().kgQuery().minGeneDisorderScore();
        NeDRexKGGraph questionGraph = neDRexKGTool.decomposeToNodes(question, "");
        List<NeDRexKGNodeEnhanced> enhancedNodes = nedrexKgQueryService.enhanceGraph(questionGraph);
        String enhancedNodesString = neDRexKGTool.stringifyEnhancedNodesToHTML(enhancedNodes);
        String oldQuery = "";
        String newQuery = "";
        final int maxAttempts = config.tools().kgQuery().retries();
        AiCypher cypher = new AiCypher();
        for (int i = 0; i < maxAttempts; i++) {
            try {
                cypher.moveCypherToHistory();
                newQuery = nedrexKGBot.generateCypherQuery(question, enhancedNodesString, oldQuery, minScore);
                oldQuery += "\n " + i + ". " + newQuery;
                cypher.setAttempts(i);
                cypher.setCypher(newQuery);
                List<Map<String, String>> results = query(newQuery);
                cypher.setResults(results);
                return cypher;
            } catch (ClientWebApplicationException e) {
                cypher.addError(e.getMessage());
                Log.errorf("Failed to query: %s (%s)", newQuery, e.getMessage());
            } catch (Exception e) {
                cypher.addError(e.getMessage());
                Log.warnf(e, "Attempt %d: Failed to generate answer for question: %s", i + 1, question);
            }

        }
        return cypher;
    }

    private List<CypherQuestion> loadQuestions(String topic) {
        String path = "tools/nedrexkg/assessed_" + topic + ".json";
        return JsonLoader.loadJson(path,
                new TypeReference<List<CypherQuestion>>() {
                });
    }

    public List<Map<String, String>> query(String cypher) {
        try {
            String json = neDRexApiClient.runQuery(cypher);
            if (json == null || json.isBlank()) return List.of();

            List<Map<String, Object>> rowsObj = mapper.readValue(
                    json, new TypeReference<>() {
                    }
            );

            List<Map<String, String>> normalized = new ArrayList<>();
            for (Map<String, Object> row : rowsObj) {
                Map<String, String> norm = new LinkedHashMap<>();
                for (var e : row.entrySet()) {
                    String key = normalizeKey(e.getKey());
                    Object val = e.getValue();

                    if (val instanceof String s) {
                        norm.put(key, normalizeValue(s));
                    }
                }
                normalized.add(norm);
            }
            return normalized;
        } catch (Exception e) {
            throw new RuntimeException("Failed to query NeDRex: " + e.getMessage(), e);
        }
    }

    private static String normalizeKey(String key) {
        return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeValue(String val) {
        if (val == null) return "";
        String s = val.trim();
        return Normalizer.normalize(s, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }

}
