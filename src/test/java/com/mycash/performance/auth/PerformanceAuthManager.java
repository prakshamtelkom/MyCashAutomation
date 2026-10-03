package com.mycash.performance.auth;

import com.mycash.performance.config.PerformanceConfig;
import com.mycash.performance.model.PerformanceApi;
import io.restassured.response.Response;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static io.restassured.RestAssured.given;

/**
 * Authentication used only by the performance suite.
 *
 * This class intentionally does NOT use ConfigReader/TokenManager so that
 * performance execution cannot mutate or depend on the functional suite's
 * global configuration state.
 */
public final class PerformanceAuthManager {

    private PerformanceAuthManager() {}

    public static String generateToken(PerformanceConfig config) {
        String configuredToken = config.getBearerToken();
        if (configuredToken != null && !configuredToken.isBlank()) {
            System.out.println("Using configured performance bearer token.");
            return stripBearer(configuredToken.trim());
        }

        Properties p = loadProperties();
        String baseUrl = required(config.getBaseUrl(), "perf.baseUrl");
        String authUrl = property(p, "auth_url", "/authenticate-admin");
        String otpUrl = property(p, "otp_url", "/verify-admin-otp");
        String user = config.getUser();

        String id = required(p, user + ".id");
        String email = required(p, user + ".email");
        String password = required(p, user + ".password");
        String deviceType = required(p, user + ".deviceType");
        String deviceId = required(p, user + ".deviceId");
        String msisdn = required(p, user + ".msisdn");
        String userType = required(p, user + ".userType");
        String pincode = property(p, user + ".pincode", property(p, user + ".pinCode", "9999"));

        JSONObject authPayload = new JSONObject()
                .put("id", Integer.parseInt(id))
                .put("email", email)
                .put("password", password)
                .put("deviceType", deviceType)
                .put("deviceId", deviceId)
                .put("msisdn", msisdn)
                .put("userType", userType);

        printConfig(baseUrl, authUrl, otpUrl, user);
        System.out.println("Performance auth source: performance.properties");

        Response authResponse = postWithRetry(baseUrl, authUrl, authPayload.toString(), config);

        if (authResponse.statusCode() < 200 || authResponse.statusCode() >= 300) {
            throw authFailure("AUTHENTICATE-ADMIN", authResponse);
        }

        Map<String, String> cookies = authResponse.getCookies();

        // The existing working functional flow uses the admin email as the
        // msisdn value for OTP verification. Keep that behavior unchanged.
        JSONObject otpPayload = new JSONObject()
                .put("msisdn", email)
                .put("pincode", pincode)
                .put("deviceId", deviceId);

        Response otpResponse = given()
                .baseUri(baseUrl)
                .basePath(otpUrl)
                .header("Accept", "application/json")
                .contentType("application/json")
                .cookies(cookies)
                .body(otpPayload.toString())
                .post();

        if (otpResponse.statusCode() < 200 || otpResponse.statusCode() >= 300) {
            throw authFailure("VERIFY-ADMIN-OTP", otpResponse);
        }

        String token = firstNonBlank(
                otpResponse.jsonPath().getString("token"),
                otpResponse.jsonPath().getString("accessToken"),
                otpResponse.jsonPath().getString("data.token"),
                otpResponse.jsonPath().getString("data.accessToken"),
                otpResponse.jsonPath().getString("entity.token"),
                otpResponse.jsonPath().getString("entity.accessToken")
        );

        if (token == null || token.isBlank()) {
            throw new RuntimeException(
                    "TOKEN NOT FOUND in VERIFY-ADMIN-OTP response\nResponse:\n"
                            + otpResponse.asPrettyString());
        }

        System.out.println("\n==========================");
        System.out.println("PERFORMANCE LOGIN SUCCESS");
        System.out.println("User: " + user);
        System.out.println("==========================\n");

        String normalizedToken = stripBearer(token);

        System.out.println("Token generated successfully (length=" + normalizedToken.length()
                + ", prefix=" + safePrefix(normalizedToken) + ")");

        return normalizedToken;
    }

    /**
     * Validates the generated performance token against one of the actual APIs
     * before JMeter starts. Only authentication/authorization failures (401/403)
     * fail fast. Other API responses are reported as request-level results so a
     * bad test payload does not get misdiagnosed as an authentication problem.
     */
    public static void validateTokenAgainstApi(PerformanceConfig config, PerformanceApi performanceApi, String token) {
        if (!config.isAuthRequired()) {
            return;
        }
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("PERFORMANCE AUTH PREFLIGHT FAILED: token is empty.");
        }

        var api = performanceApi.api();
        String url = join(config.getBaseUrl(), api.getEndPoint());
        String method = api.getMethod() == null ? "GET" : api.getMethod().trim().toUpperCase();
        String body = api.getRequestBody() == null ? "{}" : api.getRequestBody();

        System.out.println("\n========== PERFORMANCE AUTH PREFLIGHT ==========");
        System.out.println("API    : " + api.getApiName());
        System.out.println("Method : " + method);
        System.out.println("URL    : " + url);
        System.out.println("Token  : Bearer <hidden>, length=" + token.length());
        System.out.println("===============================================");

        Response response;
        var request = given()
                .header("Authorization", "Bearer " + stripBearer(token))
                .header("Accept", "application/json")
                .contentType("application/json");

        switch (method) {
            case "GET", "DELETE", "PATCH", "HEAD", "OPTIONS" -> response = request.request(method, url);
            case "POST" -> response = request.body(body).post(url);
            case "PUT" -> response = request.body(body).put(url);
            default -> throw new IllegalArgumentException(
                    "Unsupported HTTP method for performance auth preflight: " + method);
        }

        int status = response.statusCode();
        if (status == 401 || status == 403) {
            throw new RuntimeException(
                    "PERFORMANCE AUTH PREFLIGHT FAILED: generated token was rejected by the API."
                            + "\nStatus: " + status
                            + "\nAPI: " + api.getApiName()
                            + "\nResponse: " + safeResponse(response));
        }

        if (status >= 200 && status < 300) {
            System.out.println("PREFLIGHT RESULT : PASS (HTTP " + status + ")");
            System.out.println("Authentication token was accepted by the selected API.");
        } else {
            System.out.println("PREFLIGHT RESULT : AUTHENTICATION ACCEPTED / API RESPONSE HTTP " + status);
            System.out.println("The API did not return 401/403, so the token was not rejected as unauthorized.");
            System.out.println("Response: " + safeResponse(response));
        }
        System.out.println("===============================================\n");
    }

    private static String join(String baseUrl, String endpoint) {
        if (endpoint == null || endpoint.isBlank()) return baseUrl;
        String base = baseUrl == null ? "" : baseUrl.trim();
        String path = endpoint.trim();
        if (path.matches("(?i)^https?://.*")) return path;
        if (base.endsWith("/") && path.startsWith("/")) return base.substring(0, base.length() - 1) + path;
        if (!base.endsWith("/") && !path.startsWith("/")) return base + "/" + path;
        return base + path;
    }

    private static String safePrefix(String token) {
        if (token == null || token.isBlank()) return "<empty>";
        return token.substring(0, Math.min(10, token.length())) + "...";
    }

    private static String safeResponse(Response response) {
        String body = response.asString();
        if (body == null || body.isBlank()) return "<empty>";
        return body.length() > 1000 ? body.substring(0, 1000) + "..." : body;
    }

    private static Response postWithRetry(String baseUrl, String path, String body,
                                          PerformanceConfig config) {
        int attempts = Math.max(1, config.getAuthRetryCount() + 1);
        Response last = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            if (attempt > 1) {
                sleep(config.getAuthRetryDelayMs());
                System.out.println("Retrying performance authentication (attempt "
                        + attempt + "/" + attempts + ")...");
            }

            last = given()
                    .baseUri(baseUrl)
                    .basePath(path)
                    .header("Accept", "application/json")
                    .contentType("application/json")
                    .body(body)
                    .post();

            if (last.statusCode() >= 200 && last.statusCode() < 300) {
                return last;
            }
        }

        return last;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("performance.properties")) {
            if (is == null) {
                throw new IllegalStateException("performance.properties not found on test classpath");
            }
            properties.load(is);
            return properties;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load performance.properties", e);
        }
    }

    private static String property(Properties p, String key, String defaultValue) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) v = p.getProperty(key);
        return (v == null || v.isBlank()) ? defaultValue : v.trim();
    }

    private static String required(Properties p, String key) {
        String v = property(p, key, "");
        if (v.isBlank()) throw new IllegalStateException("Missing performance auth property: " + key);
        return v;
    }

    private static String required(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing performance configuration: " + key);
        }
        return value.trim();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value.trim();
        }
        return null;
    }

    private static String stripBearer(String token) {
        return token.regionMatches(true, 0, "Bearer ", 0, 7)
                ? token.substring(7).trim()
                : token;
    }

    private static RuntimeException authFailure(String step, Response response) {
        return new RuntimeException(
                "PERFORMANCE AUTH FAILED at " + step
                        + "\nStatus: " + response.statusCode()
                        + "\nResponse:\n" + response.asPrettyString());
    }

    private static void sleep(long millis) {
        if (millis <= 0) return;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while retrying performance authentication", e);
        }
    }

    private static void printConfig(String baseUrl, String authUrl, String otpUrl, String user) {
        System.out.println("\n========== PERFORMANCE AUTH CONFIG ==========");
        System.out.println("User     : " + user);
        System.out.println("Base URL : " + baseUrl);
        System.out.println("Auth URL : " + authUrl);
        System.out.println("OTP URL  : " + otpUrl);
        System.out.println("============================================\n");
    }
}
