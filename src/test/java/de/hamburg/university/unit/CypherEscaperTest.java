package de.hamburg.university.unit;

import de.hamburg.university.helper.cypher.CypherEscaper;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CypherEscaperTest {


    static Stream<String> safeQueries() {
        return Stream.of(
                // 1) no string literal at all
                "MATCH (n) RETURN n",

                "MATCH (n) WHERE n.name = $name RETURN n",

                // 3) empty string literal
                "MATCH (n) WHERE n.name = '' RETURN n",

                // 4) normal string literal
                "MATCH (n) WHERE n.name = 'Alice' RETURN n",

                // 5) multiple literals, all normal
                "MATCH (n) WHERE n.a = 'x' AND n.b = 'y' RETURN n",

                // 6) doubled quote inside string (Cypher-style escaping)
                "MATCH (n) WHERE n.name = 'O''Reilly' RETURN n",

                // 7) backslash escaped quote inside string (if you support it)
                "MATCH (n) WHERE n.name = 'O\\'Reilly' RETURN n",

                // 8) quoted value containing backslashes
                "MATCH (n) WHERE n.path = 'C:\\\\temp\\\\file' RETURN n",

                // 9) string literal + numbers
                "MATCH (n) WHERE n.age > 18 AND n.city = 'Hamburg' RETURN n",

                // 10) list of safe strings
                "MATCH (n) WHERE n.tag IN ['a', 'b', 'c'] RETURN n"
        );
    }

    static Stream<String> unsafeQueries() {
        return Stream.of(
                // 1) classic broken literal
                "MATCH (n) WHERE n.name = 'O'Reilly' RETURN n",

                // 2) another broken literal
                "MATCH (n) WHERE n.title = 'John's book' RETURN n",

                // 3) broken in the middle
                "MATCH (n) WHERE n.note = 'He said 'hello'' RETURN n",

                // 4) broken after some text
                "MATCH (n) WHERE n.comment = 'abc'def' RETURN n",

                // 5) broken with AND after it
                "MATCH (n) WHERE n.a = 'x'y' AND n.b = 'z' RETURN n",

                // 6) broken in list literal
                "MATCH (n) WHERE n.tag IN ['a', 'b'c', 'd'] RETURN n",

                // 7) broken in map literal
                "CREATE (n {name: 'O'Reilly'}) RETURN n",

                // 8) broken with RETURN
                "RETURN 'O'Reilly' AS x",

                // 9) broken in SET
                "MATCH (n) SET n.name = 'John's' RETURN n",

                // 10) broken with punctuation
                "MATCH (n) WHERE n.text = 'it'll fail' RETURN n"
        );
    }

    @ParameterizedTest(name = "SAFE should NOT be flagged: {0}")
    @MethodSource("safeQueries")
    @DisplayName("10 correct queries: should not require escaping")
    void safe_queries_should_not_be_flagged(String cypher) {
        assertFalse(CypherEscaper.hasUnescapedQuotes(cypher),
                () -> "Query was incorrectly flagged as unsafe: " + cypher);

        // escapeIfNeeded should be idempotent for safe queries
        assertEquals(cypher, CypherEscaper.escapeIfNeeded(cypher),
                () -> "Safe query changed unexpectedly: " + cypher);
    }

    @ParameterizedTest(name = "UNSAFE should be flagged: {0}")
    @MethodSource("unsafeQueries")
    @DisplayName("10 false queries: should be detected and escaped")
    void unsafe_queries_should_be_flagged_and_escaped(String cypher) {
        assertTrue(CypherEscaper.hasUnescapedQuotes(cypher),
                () -> "Unsafe query was NOT detected: " + cypher);

        String escaped = CypherEscaper.escapeIfNeeded(cypher);

        // After escaping, it should not be flagged anymore
        assertFalse(CypherEscaper.hasUnescapedQuotes(escaped),
                () -> "Escaped query is still unsafe: " + escaped);

        // Escaping should actually change the query for unsafe input
        assertNotEquals(cypher, escaped,
                () -> "Unsafe query was not modified by escaping: " + cypher);
    }

    @Test
    @DisplayName("Sanity: escapeIfNeeded is stable (escaping twice doesn't change again)")
    void escape_should_be_idempotent() {
        String q = "MATCH (n) WHERE n.name = 'O'Reilly' RETURN n";
        String once = CypherEscaper.escapeIfNeeded(q);
        String twice = CypherEscaper.escapeIfNeeded(once);
        assertEquals(once, twice, "Escaping should be idempotent");
    }
}