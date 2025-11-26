package de.hamburg.university.agent.provider;

import java.util.function.Supplier;

public final class ChatRequestContext {

    private static final ThreadLocal<String> current = new ThreadLocal<>();

    public static void set(String id) {
        current.set(id);
    }

    public static String get() {
        return current.get();
    }

    public static void clear() {
        current.remove();
    }

    public static <T> T with(String id, Supplier<T> supplier) {
        try {
            current.set(id);
            return supplier.get();
        } finally {
            current.remove();
        }
    }
}