package com.mycash.core.Utills;

import com.mycash.core.config.ConfigReader;

public final class ApiUrlBuilder {

    private static final String BASE_URL;

    static {
        BASE_URL = ConfigReader.get("baseuri")
                .replaceAll("/+$", "");

        if (BASE_URL == null || BASE_URL.isEmpty()) {
            throw new RuntimeException("baseuri missing in config");
        }
    }

    private ApiUrlBuilder() {}

    public static String url(String endpoint) {

        if (endpoint == null || endpoint.isEmpty()) {
            throw new RuntimeException("Endpoint is empty");
        }

        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }

        return BASE_URL + endpoint;
    }
}