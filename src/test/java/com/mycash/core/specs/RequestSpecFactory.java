package com.mycash.core.specs;

import com.mycash.core.auth.AuthCache;
import com.mycash.core.config.ConfigReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class RequestSpecFactory {

    private static final String DEFAULT_USER = "admin";

    private RequestSpecFactory() {
    }

    /**
     * Default Request Specification
     * Uses admin authentication.
     */
    public static RequestSpecification getRequestSpec() {

        return getRequestSpec(DEFAULT_USER, true);

    }

    /**
     * Authenticated request for specific user.
     */
    public static RequestSpecification getRequestSpec(String userKey) {

        return getRequestSpec(userKey, true);

    }

    /**
     * Request specification with optional authentication.
     */
    public static RequestSpecification getRequestSpec(String userKey,
                                                      boolean authenticationRequired) {

        if (userKey == null || userKey.isBlank()) {
            userKey = DEFAULT_USER;
        }

        String baseUrl = ConfigReader.get("baseUrl");

        if (baseUrl == null || baseUrl.isBlank()) {

            throw new RuntimeException(
                    "baseUrl is missing from qa.properties");

        }

        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .addHeader("Accept", "application/json");

        if (authenticationRequired) {

            String token = AuthCache.getCachedToken(userKey);

            if (token == null || token.isBlank()) {

                throw new RuntimeException(
                        "Authentication token is empty for user : " + userKey);

            }

            builder.addHeader("Authorization", "Bearer " + token);

            System.out.println("----------------------------------------");
            System.out.println("Using Authentication");
            System.out.println("User      : " + userKey);
            System.out.println("Base URL  : " + baseUrl);
            System.out.println("JWT Length: " + token.length());
            System.out.println("----------------------------------------");

        } else {

            System.out.println("----------------------------------------");
            System.out.println("Anonymous Request");
            System.out.println("Base URL : " + baseUrl);
            System.out.println("----------------------------------------");

        }

        return builder.build();

    }

    /**
     * Returns a request specification without Authorization.
     */
    public static RequestSpecification getAnonymousRequestSpec() {

        return getRequestSpec(DEFAULT_USER, false);

    }

}