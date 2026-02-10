package de.hamburg.university.agent.tool.nedrex;

public enum NeDRexToolTypes {
    CLOSENESS,
    DIAMOND,
    TRUSTRANK;

    public static boolean isCloseness(NeDRexToolTypes type) {
        if (type == null) {
            return false;
        }
        return type == CLOSENESS;
    }

    public static boolean isDiamond(NeDRexToolTypes type) {
        if (type == null) {
            return false;
        }
        return type == DIAMOND;
    }

    public static boolean isTrustRank(NeDRexToolTypes type) {
        if (type == null) {
            return false;
        }
        return type == TRUSTRANK;
    }

    public static boolean isClosenessOrTrustRank(NeDRexToolTypes type) {
        return isCloseness(type) || isTrustRank(type);
    }
}
