package de.hamburg.university.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class JsonLoader {
    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();


    public static <T> T loadJson(String path, TypeReference<T> typeReference) {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path)) {

            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }

            return mapper.readValue(is, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON from " + path, e);
        }
    }
}
