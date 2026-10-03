package com.mycash.core.Utills;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Map;

public class TestDataLoader {

    public static Map<String, Object> load(String fileName) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            InputStream inputStream = TestDataLoader.class
                    .getClassLoader()
                    .getResourceAsStream(fileName);

            if (inputStream == null) {
                throw new RuntimeException("File not found in classpath: " + fileName);
            }

            return mapper.readValue(inputStream, Map.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data: " + fileName, e);
        }
    }
}