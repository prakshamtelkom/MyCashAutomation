package com.mycash.core.auth;

import com.mycash.core.config.ConfigReader;
import io.restassured.response.Response;
import org.json.JSONObject;

import java.util.Map;

import static io.restassured.RestAssured.given;

public final class TokenManager {

    private TokenManager() {}

    public static String generateToken(String userKey) {

        if (userKey == null || userKey.isBlank()) {
            userKey = "admin";
        }

        String baseUrl = require("baseUrl");
        String authUrl = require("auth_url");
        String otpUrl = require("otp_url");

        printConfig(baseUrl, authUrl, otpUrl, userKey);

        // =========================
        // STEP 1: AUTH REQUEST
        // =========================
        JSONObject authPayload = buildAuthPayload(userKey);

        Response authResponse = given()
                .baseUri(baseUrl)
                .basePath(authUrl)
                .contentType("application/json")
                .body(authPayload.toString())
                .log().all()
                .post();

        if (authResponse.statusCode() != 200) {
            throw new RuntimeException(
                    "AUTH FAILED\nStatus: " + authResponse.statusCode()
                            + "\nResponse:\n" + authResponse.asPrettyString());
        }

        Map<String, String> cookies = authResponse.getCookies();

        // =========================
        // STEP 2: BUILD OTP REQUEST (FIXED)
        // =========================
        JSONObject otpPayload = buildOtpPayload(userKey);

        // =========================
        // STEP 3: OTP VERIFY REQUEST
        // =========================
        Response otpResponse = given()
                .baseUri(baseUrl)
                .basePath(otpUrl)
                .contentType("application/json")
                .cookies(cookies)
                .body(otpPayload.toString())
                .log().all()
                .post();

        if (otpResponse.statusCode() != 200) {
            throw new RuntimeException(
                    "OTP VERIFICATION FAILED\nStatus: " + otpResponse.statusCode()
                            + "\nResponse:\n" + otpResponse.asPrettyString());
        }

        // =========================
        // STEP 4: EXTRACT TOKEN
        // =========================
        String token = otpResponse.jsonPath().getString("token");

        if (token == null || token.isBlank()) {
            throw new RuntimeException("TOKEN NOT FOUND in OTP response");
        }

        System.out.println("\n==========================");
        System.out.println("LOGIN SUCCESS");
        System.out.println("User: " + userKey);
        System.out.println("==========================\n");

        return token;
    }

    // =========================
    // AUTH PAYLOAD
    // =========================
    private static JSONObject buildAuthPayload(String userKey) {
        return new JSONObject()
                .put("id", Integer.parseInt(require(userKey + ".id")))
                .put("email", require(userKey + ".email"))
                .put("password", require(userKey + ".password"))
                .put("deviceType", require(userKey + ".deviceType"))
                .put("deviceId", require(userKey + ".deviceId"))
                .put("msisdn", require(userKey + ".msisdn"))
                .put("userType", require(userKey + ".userType"));
    }

    // =========================
    // OTP PAYLOAD (IMPORTANT FIX)
    // =========================
    private static JSONObject buildOtpPayload(String userKey) {

        // IMPORTANT:
        // msisdn = email (as per your API)
        // pincode = static OR config
        // deviceId = same as auth

        return new JSONObject()
                .put("msisdn", require(userKey + ".email"))
                .put("pincode", getOrDefault(userKey + ".pincode", "9999"))
                .put("deviceId", require(userKey + ".deviceId"));
    }

    // =========================
    // CONFIG HELPERS
    // =========================
    private static String require(String key) {
        String value = ConfigReader.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing property: " + key);
        }
        return value.trim();
    }

    private static String getOrDefault(String key, String def) {
        String value = ConfigReader.get(key);
        return (value == null || value.isBlank()) ? def : value.trim();
    }

    private static void printConfig(String baseUrl, String authUrl, String otpUrl, String user) {
        System.out.println("\n========== AUTH CONFIG ==========");
        System.out.println("User     : " + user);
        System.out.println("Base URL : " + baseUrl);
        System.out.println("Auth URL : " + authUrl);
        System.out.println("OTP URL  : " + otpUrl);
        System.out.println("================================\n");
    }
}