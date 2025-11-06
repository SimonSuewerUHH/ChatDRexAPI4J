package de.hamburg.university.unit;

import de.hamburg.university.helper.drugstone.cypher.CypherResultIdExtractor;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

@QuarkusTest
public class CypherResultIdExtractorTest {


    @Test
    void testExtractIdsWithValidInput() {
        String inputString = "[{\"geneId\": \"entrez.6139\"}, {\"anotherId\": \"test.12345\"}, {\"noMatch\": \"value\"}]";
        List<String> expectedIds = Arrays.asList("entrez.6139", "test.12345");

        List<String> actualIds = CypherResultIdExtractor.extractResults(inputString);

        Assertions.assertEquals(expectedIds.size(), actualIds.size(), "The number of extracted IDs should match the expected count.");
        Assertions.assertEquals(expectedIds, actualIds, "The extracted IDs should match the expected list.");
    }

    @Test
    void testExtractIdsWithNoMatches() {
        String inputString = "[{\"geneName\": \"RPL17\"}, {\"otherKey\": \"value\"}]";

        List<String> actualIds = CypherResultIdExtractor.extractResults(inputString);

        Assertions.assertTrue(actualIds.isEmpty(), "The list of IDs should be empty when there are no matches.");
    }

    @Test
    void testExtractIdsWithEmptyInput() {
        String inputString = "";

        List<String> actualIds = CypherResultIdExtractor.extractResults(inputString);

        Assertions.assertTrue(actualIds.isEmpty(), "The list of IDs should be empty for an empty input string.");
    }

    @Test
    void testWithFullOriginalString() {
        // Arrange
        String inputString = "[{\"geneId\": \"entrez.6139\", \"geneName\": \"RPL17\"}, {\"geneId\": \"entrez.6173\", \"geneName\": \"RPL36A\"}, " +
                "{\"geneId\": \"entrez.6234\", \"geneName\": \"RPS28\"}, {\"geneId\": \"entrez.6222\", \"geneName\": \"RPS18\"}, " +
                "{\"geneId\": \"entrez.6171\", \"geneName\": \"RPL41\"}, {\"geneId\": \"entrez.6235\", \"geneName\": \"RPS29\"}, " +
                "{\"geneId\": \"entrez.6170\", \"geneName\": \"RPL39\"}, {\"geneId\": \"entrez.4736\", \"geneName\": \"RPL10A\"}, " +
                "{\"anotherId\": \"test.12345\", \"geneName\": \"TEST\"}, " +
                "{\"geneId\": \"entrez.6147\", \"geneName\": \"RPL23A\"}, {\"geneId\": \"entrez.6191\", \"geneName\": \"RPS4X\"}, " +
                "{\"geneId\": \"entrez.6136\", \"geneName\": \"RPL12\"}, {\"geneId\": \"entrez.6168\", \"geneName\": \"RPL37A\"}, " +
                "{\"geneId\": \"entrez.6231\", \"geneName\": \"RPS26\"}, {\"geneId\": \"entrez.9045\", \"geneName\": \"RPL14\"}, " +
                "{\"geneId\": \"entrez.6228\", \"geneName\": \"RPS28\"}]";

        List<String> expectedIds = Arrays.asList(
                "entrez.6139", "entrez.6173", "entrez.6234", "entrez.6222", "entrez.6171",
                "entrez.6235", "entrez.6170", "entrez.4736", "test.12345", "entrez.6147",
                "entrez.6191", "entrez.6136", "entrez.6168", "entrez.6231", "entrez.9045",
                "entrez.6228"
        );

        List<String> actualIds = CypherResultIdExtractor.extractResults(inputString);

        Assertions.assertEquals(expectedIds, actualIds, "The extracted IDs from the full string should match the expected list.");
    }
}
