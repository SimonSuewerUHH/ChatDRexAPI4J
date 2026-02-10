package de.hamburg.university.helper.cypher;

public  class CypherEscaper {

    /**
     * Returns true if the query contains an unescaped single quote (apostrophe)
     * INSIDE a Cypher single-quoted string literal.
     *
     * Recognized escapes inside string literals:
     *  - Cypher style: ''   (two single quotes)
     *  - Backslash style: \' (kept as "escaped" for compatibility)
     */
    public static boolean hasUnescapedQuotes(String cypher) {
        if (cypher == null || cypher.isEmpty()) return false;

        boolean inString = false;

        for (int i = 0; i < cypher.length(); i++) {
            char c = cypher.charAt(i);

            if (!inString) {
                if (c == '\'') {
                    inString = true;
                }
                continue;
            }

            // inString == true
            if (c == '\\') {
                // Treat \' as escaped quote (compat)
                if (i + 1 < cypher.length() && cypher.charAt(i + 1) == '\'') {
                    i++; // skip the escaped quote char
                }
                continue;
            }

            if (c == '\'') {
                // Cypher escape: '' -> escaped quote inside string
                if (i + 1 < cypher.length() && cypher.charAt(i + 1) == '\'') {
                    i++; // consume second quote
                    continue;
                }

                // single quote not doubled: either closing quote OR unescaped apostrophe
                if (isLikelyClosingDelimiter(peek(cypher, i + 1))) {
                    inString = false; // closing quote
                } else {
                    // looks like "O'Reilly" => unescaped apostrophe inside string
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Escapes unescaped apostrophes inside Cypher string literals using Cypher-standard ''.
     * Leaves already-escaped quotes intact ('' and \').
     */
    public static String escapeIfNeeded(String cypher) {
        if (cypher == null || cypher.isEmpty()) return cypher;
        if (!hasUnescapedQuotes(cypher)) return cypher;

        StringBuilder out = new StringBuilder(cypher.length() + 16);
        boolean inString = false;

        for (int i = 0; i < cypher.length(); i++) {
            char c = cypher.charAt(i);

            if (!inString) {
                out.append(c);
                if (c == '\'') {
                    inString = true;
                }
                continue;
            }

            // inString == true
            if (c == '\\') {
                // keep backslash escapes as-is
                out.append(c);
                if (i + 1 < cypher.length() && cypher.charAt(i + 1) == '\'') {
                    out.append('\'');
                    i++;
                }
                continue;
            }

            if (c == '\'') {
                // already escaped as ''
                if (i + 1 < cypher.length() && cypher.charAt(i + 1) == '\'') {
                    out.append("''");
                    i++;
                    continue;
                }

                // decide closing vs apostrophe
                char next = peek(cypher, i + 1);
                if (isLikelyClosingDelimiter(next)) {
                    out.append('\'');
                    inString = false;
                } else {
                    // unescaped apostrophe: escape as ''
                    out.append("''");
                }
                continue;
            }

            out.append(c);
        }

        return out.toString();
    }

    private static char peek(String s, int idx) {
        return (idx >= 0 && idx < s.length()) ? s.charAt(idx) : '\0';
    }

    /**
     * Heuristic:
     * If after a ' we see something that typically ends/continues an expression,
     * we treat it as a closing quote.
     *
     * If after a ' we see an identifier-like char (letter/digit/_),
     * we treat it as an apostrophe inside the string (needs escaping).
     */
    private static boolean isLikelyClosingDelimiter(char next) {
        if (next == '\0') return true; // end of string => must be closing

        // whitespace ends a token => likely closing
        if (Character.isWhitespace(next)) return true;

        // common Cypher delimiters / operators / punctuation
        return switch (next) {
            case ',', ')', ']', '}', ':', ';',
                 '+', '-', '*', '/', '%',
                 '=', '<', '>', '!', '?',
                 '|', '&', '.', '\n', '\r', '\t' -> true;
            default -> false;
        };
    }
}