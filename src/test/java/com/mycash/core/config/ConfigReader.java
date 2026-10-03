package com.mycash.core.config;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public final class ConfigReader {

    private static final Properties PROPERTIES = new Properties();

    private ConfigReader() {}

    public static void load(String fileName) {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(fileName)) {

            if (is == null) {
                throw new RuntimeException("Config not found in classpath: " + fileName);
            }

            PROPERTIES.clear();
            PROPERTIES.load(is);

            System.out.println("Loaded config keys = " + PROPERTIES.size());

        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + fileName, e);
        }
    }

    public static String get(String key) {
        String value = PROPERTIES.getProperty(key);
        return (value != null) ? value.trim() : null;
    }

    public static List<String> getUserList(String env) {

        String users = get(env + ".users");

        if (isBlank(users)) {
            users = get("users");
        }

        if (isBlank(users)) {
            users = "admin";
        }

        return Arrays.stream(users.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static boolean isLoaded() {
        return !PROPERTIES.isEmpty();
    }

    private static boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }

    public static void printAll() {
        PROPERTIES.forEach((k, v) -> System.out.println(k + " = " + v));
    }
}