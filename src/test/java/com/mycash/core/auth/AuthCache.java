package com.mycash.core.auth;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthCache {

    /**
     * Token validity in cache.
     * Keep it slightly lower than the actual JWT expiry.
     */
    private static final long TOKEN_CACHE_SECONDS = 3300;

    private static final ConcurrentHashMap<String, CachedToken> CACHE =
            new ConcurrentHashMap<>();

    private AuthCache() {
    }

    private static class CachedToken {

        private final String token;
        private final Instant expiresAt;

        CachedToken(String token, Instant expiresAt) {
            this.token = token;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }

        String getToken() {
            return token;
        }
    }

    /**
     * Returns a valid cached token.
     * Generates a new token if missing or expired.
     */
    public static synchronized String getCachedToken(String userKey) {

        if (userKey == null || userKey.isBlank()) {
            userKey = "admin";
        }

        CachedToken cached = CACHE.get(userKey);

        if (cached != null && !cached.isExpired()) {

            System.out.println("---------------------------------------");
            System.out.println("Using Cached Token : " + userKey);
            System.out.println("---------------------------------------");

            return cached.getToken();
        }

        System.out.println("---------------------------------------");
        System.out.println("Generating New Token : " + userKey);
        System.out.println("---------------------------------------");

        String token = TokenManager.generateToken(userKey);

        cacheToken(userKey, token);

        return token;
    }

    /**
     * Store a token manually.
     */
    public static synchronized void cacheToken(String userKey,
                                               String token) {

        if (userKey == null || userKey.isBlank()) {
            userKey = "admin";
        }

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Cannot cache empty token.");
        }

        CACHE.put(
                userKey,
                new CachedToken(
                        token,
                        Instant.now().plusSeconds(TOKEN_CACHE_SECONDS)
                )
        );

        System.out.println("---------------------------------------");
        System.out.println("Token Cached");
        System.out.println("User : " + userKey);
        System.out.println("---------------------------------------");
    }

    /**
     * Check whether a valid token exists.
     */
    public static boolean hasValidToken(String userKey) {

        CachedToken cached = CACHE.get(userKey);

        return cached != null && !cached.isExpired();
    }

    /**
     * Remove one user's token.
     */
    public static void clear(String userKey) {

        CACHE.remove(userKey);

        System.out.println("Removed cached token : " + userKey);
    }

    /**
     * Remove every cached token.
     */
    public static void clearAll() {

        CACHE.clear();

        System.out.println("Cleared complete token cache.");
    }

    /**
     * Print cache contents for debugging.
     */
    public static void printCache() {

        System.out.println("\n========== TOKEN CACHE ==========");

        if (CACHE.isEmpty()) {

            System.out.println("No cached tokens.");

        } else {

            CACHE.forEach((user, token) -> {

                System.out.println(
                        user +
                                " -> expires @ " +
                                token.expiresAt
                );

            });

        }

        System.out.println("=================================\n");
    }
}