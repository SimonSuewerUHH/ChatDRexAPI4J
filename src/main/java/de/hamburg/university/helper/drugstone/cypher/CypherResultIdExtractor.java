package de.hamburg.university.helper.drugstone.cypher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CypherResultIdExtractor {

    public static List<String> extractResults(String result) {
        final String regex = "\"[^\"]*d\"\\s*:\\s*\"([^\"]+)\"";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(result);

        final List<String> extractedIds = new ArrayList<>();
        while (matcher.find()) {
            extractedIds.add(matcher.group(1));
        }
        return extractedIds;
    }
}
