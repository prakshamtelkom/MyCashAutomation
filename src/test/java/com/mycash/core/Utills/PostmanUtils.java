package com.mycash.core.Utills;

import java.util.Map;

public class PostmanUtils {

    public static String resolve(String input, Map<String, String> vars) {
        if (input == null) return null;

        for (Map.Entry<String, String> e : vars.entrySet()) {
            input = input.replace("{{" + e.getKey() + "}}", e.getValue());
        }
        return input;
    }
}