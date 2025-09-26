package de.hamburg.university.tool;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.university.agent.bot.DrugstOneBot;
import de.hamburg.university.helper.JsonLoader;
import de.hamburg.university.helper.LoggingProgressBar;
import de.hamburg.university.helper.drugstone.dto.DrugstOneConfigDTO;
import de.hamburg.university.helper.drugstone.dto.DrugstOneGroupsConfigDTO;
import de.hamburg.university.tool.pojo.DrugstOneConfigQuestion;
import de.hamburg.university.tool.pojo.ResearchResult;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class DrugstOneConfigEvaluationTest {
    @Inject
    DrugstOneBot bot;

    private static final Logger LOG = Logger.getLogger(DrugstOneConfigEvaluationTest.class);


    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DrugstOneConfigDTO defaults() {
        return new DrugstOneConfigDTO();
    }

    private DrugstOneConfigDTO call(String instruction) {
        return bot.updateConfig(defaults(), instruction);
    }

    @Test
    void toggleShowViewsOff() {
        DrugstOneConfigDTO updated = call("Turn off showView / showViews.");
        assertFalse(updated.isShowViews(), "showViews should be false");
        // sanity: unrelated field kept
        assertEquals(defaults().getLegendPos(), updated.getLegendPos());
    }

    @Test
    void hideSidebar_unionTypedBooleanOrString() {
        DrugstOneConfigDTO base = defaults();
        assertEquals("right".equals(base.getShowNetworkMenu()) ? "right" : "left",
                base.getShowSidebar(), "Assumption: default sidebar is 'left'");

        DrugstOneConfigDTO updated = call("Hide the sidebar.");
        assertEquals(Boolean.FALSE, updated.getShowSidebar(), "showSidebar should become boolean false");
        // do not alter other union fields unless asked
        assertEquals(base.getShowNetworkMenu(), updated.getShowNetworkMenu());
    }

    @Test
    void moveLegendRight_enumLikeString() {
        DrugstOneConfigDTO updated = call("Move the legend to the right.");
        assertEquals("right", updated.getLegendPos());
        // unchanged examples
        assertEquals(defaults().isShowLegend(), updated.isShowLegend());
        assertEquals(defaults().isShowLegendNodes(), updated.isShowLegendNodes());
    }

    @Test
    void setIdentifierToEntrez() {
        DrugstOneConfigDTO updated = call("Use Entrez IDs for identifiers.");
        assertEquals("entrez", updated.getIdentifier().toLowerCase());
        // label unchanged
        assertEquals(defaults().getLabel(), updated.getLabel());
    }

    @Test
    void noHallucinatedKeys_noEffectIfUnknown() {
        DrugstOneConfigDTO base = defaults();
        DrugstOneConfigDTO updated = call("Add a new property 'superSpeed' with value 11 and enable turbo mode.");
        // Since bot must not invent keys, config should remain identical for unrelated asks
        assertEquals(base, updated, "Configuration must remain unchanged when instruction does not map to existing keys");
    }

    @Test
    void algorithmsRemainUntouchedWithoutExplicitRequest() {
        DrugstOneConfigDTO base = defaults();
        DrugstOneConfigDTO updated = call("Optimize algorithms automatically.");
        assertEquals(base.getAlgorithms(), updated.getAlgorithms(),
                "Algorithms must remain unchanged without a precise mapping to existing keys");
        // spot check another field still same
        assertEquals(base.getNetworkMenuButtonLayoutLabel(), updated.getNetworkMenuButtonLayoutLabel());
    }

    @Test
    void flipPhysicsOnAndInitialIndependently() {
        DrugstOneConfigDTO updated = call("Enable physics but disable initial layouting.");
        assertTrue(updated.isPhysicsOn(), "physicsOn should be true");
        assertFalse(updated.isPhysicsInitial(), "physicsInitial should be false");
    }

    @Test
    void switchNetworkMenuSide() {
        DrugstOneConfigDTO updated = call("Place the network menu on the left.");
        assertEquals("left", updated.getShowNetworkMenu(), "showNetworkMenu should be 'left'");
        // Sidebar remains as-is unless mentioned
        assertEquals(defaults().getShowSidebar(), updated.getShowSidebar());
    }

    @Test
    void evalQuestions() {
        List<DrugstOneConfigQuestion> questions = JsonLoader.loadJson(
                "tools/drugstone/config-test.json",
                new com.fasterxml.jackson.core.type.TypeReference<>() {
                }
        );
        LoggingProgressBar bar = LoggingProgressBar.of(LOG, "Testing", questions.size());
        List<ResearchResult> results = new ArrayList<>();
        Path out = Paths.get("results", "eval", "drugst-one-config.json");


        DrugstOneConfigDTO base = defaults();

        for (DrugstOneConfigQuestion question : questions) {
            ResearchResult result = new ResearchResult();
            try {
                result.setQuestion(question.getQuestion());

                DrugstOneConfigDTO expectedNode = deepMerge(base, question.getPartResult());

                DrugstOneConfigDTO actual = bot.updateConfig(base, question.getQuestion());

                result.setAnswer(expectedNode.toString());
                result.setAnswer(MAPPER.writeValueAsString(actual));
                if (expectedNode.equals(actual)) {
                    result.setCorrectAnswer(true);
                } else {
                    String diff = diffAsString(expectedNode, actual);
                    result.setErrorMessage("Configs differ:\n" + diff);
                }
            } catch (Exception e) {
                result.setErrorMessage(e.getMessage());
            }
            bar.step();
            results.add(result);
        }
        bar.complete();
        ResearchResult.printJsonFile(results, out);

        int correct = (int) results.stream().filter(ResearchResult::isCorrectAnswer).count();
        for (ResearchResult r : results) {
            LOG.info(r.minimizeString());
        }
        Log.info("_________________________________________");
        Log.infof("Correct answers: %d/%d", correct, results.size());
        Log.infof("JSON written: %s", out.toAbsolutePath());

    }

    @Test
    void evalQuestionsConfigGroup() {
        List<DrugstOneConfigQuestion> questions = JsonLoader.loadJson(
                "tools/drugstone/config-groups-test.json",
                new com.fasterxml.jackson.core.type.TypeReference<>() {
                }
        );
        LoggingProgressBar bar = LoggingProgressBar.of(LOG, "Testing", questions.size());
        List<ResearchResult> results = new ArrayList<>();
        Path out = Paths.get("results", "eval", "drugst-one-config-group.json");


        DrugstOneGroupsConfigDTO base = JsonLoader.loadJson(
                "tools/drugstone/config-groups-baseline2.json",
                new TypeReference<DrugstOneGroupsConfigDTO>() {
                }
        );

        for (DrugstOneConfigQuestion question : questions) {
            ResearchResult result = new ResearchResult();
            try {
                result.setQuestion(question.getQuestion());

                DrugstOneGroupsConfigDTO expectedNode = deepMerge(base, question.getPartResult());

                DrugstOneGroupsConfigDTO actual = bot.updateGroups(base, question.getQuestion());

                result.setAnswer(expectedNode.toString());
                result.setAnswer(MAPPER.writeValueAsString(actual));
                if (expectedNode.equals(actual)) {
                    result.setCorrectAnswer(true);
                } else {
                    String diff = diffAsString(expectedNode, actual);
                    result.setErrorMessage("Configs differ:\n" + diff);
                }
            } catch (Exception e) {
                result.setErrorMessage(e.getMessage());
            }
            bar.step();
            results.add(result);
        }
        bar.complete();
        ResearchResult.printJsonFile(results, out);

        int correct = (int) results.stream().filter(ResearchResult::isCorrectAnswer).count();
        for (ResearchResult r : results) {
            LOG.info(r.minimizeString());
        }
        Log.info("_________________________________________");
        Log.infof("Correct answers: %d/%d", correct, results.size());
        Log.infof("JSON written: %s", out.toAbsolutePath());

    }

    public String diffAsString(Object actual, Object other) {
        if (actual == null && other == null) {
            return "Both configs are null";
        }
        if (actual == null) {
            return "Actual config is null";
        }
        if (other == null) {
            return "Other config is null";
        }

        Map<String, Object> actualMap = MAPPER.convertValue(actual, new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> otherMap = MAPPER.convertValue(other, new TypeReference<Map<String, Object>>() {
        });

        StringBuilder sb = new StringBuilder();

        // union of keys from both maps
        Set<String> allKeys = new TreeSet<>();
        allKeys.addAll(actualMap.keySet());
        allKeys.addAll(otherMap.keySet());

        for (String key : allKeys) {
            Object val1 = actualMap.get(key);
            Object val2 = otherMap.get(key);
            if (!Objects.equals(val1, val2)) {
                sb.append(key)
                        .append(": ")
                        .append(val1)
                        .append(" -> ")
                        .append(val2)
                        .append("\n");
            }
        }

        return sb.length() == 0 ? "No differences" : sb.toString();
    }

    private DrugstOneConfigDTO deepMerge(DrugstOneConfigDTO base, Map<String, Object> partResult) {
        try {
            Map<String, Object> baseMap = MAPPER.convertValue(base, new TypeReference<>() {
            });
            Map<String, Object> merged = deepMerge(baseMap, partResult);
            return MAPPER.convertValue(merged, DrugstOneConfigDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deep merge config", e);
        }
    }

    private DrugstOneGroupsConfigDTO deepMerge(DrugstOneGroupsConfigDTO base, Map<String, Object> partResult) {
        try {
            Map<String, Object> baseMap = MAPPER.convertValue(base, new TypeReference<>() {
            });
            Map<String, Object> merged = deepMerge(baseMap, partResult);
            return MAPPER.convertValue(merged, DrugstOneGroupsConfigDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deep merge config", e);
        }
    }

    private static <K, V> Map<K, V> deepMerge(Map<K, V> base, Map<K, V> override) {
        if (override == null) {
            return base;
        }
        if (base == null) {
            return new HashMap<>(override);
        }

        for (Map.Entry<K, V> entry : override.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();

            if (value instanceof Map && base.get(key) instanceof Map) {
                // recurse into nested maps
                Map<K, V> baseChild = (Map<K, V>) base.get(key);
                Map<K, V> overrideChild = (Map<K, V>) value;
                base.put(key, (V) deepMerge(baseChild, overrideChild));
            } else {
                // overwrite scalar or list values
                base.put(key, value);
            }
        }
        return base;
    }
}
