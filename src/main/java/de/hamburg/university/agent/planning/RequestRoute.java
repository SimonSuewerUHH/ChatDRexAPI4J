package de.hamburg.university.agent.planning;

public enum RequestRoute {
    NETWORK,
    RESEARCH,
    HELP,
    UNKNOWN;

    public static RequestRoute from(String s) {
        if (s == null) return UNKNOWN;
        try {
            return RequestRoute.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }
}