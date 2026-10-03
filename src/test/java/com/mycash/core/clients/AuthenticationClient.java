package com.mycash.core.clients;

import com.mycash.core.specs.RequestSpecFactory;
import com.mycash.core.specs.ResponseSpecFactory;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class AuthenticationClient {

    public Response requestOtp(String payload) {
        return given().spec(RequestSpecFactory.getRequestSpec())
                .body(payload)
                .when().post("/request-otp")
                .then().spec(ResponseSpecFactory.ok())
                .extract().response();
    }

    public Response generateToken(String payload) {
        return given().spec(RequestSpecFactory.getRequestSpec())
                .body(payload)
                .when().post("/get-otp-login-token")
                .then().spec(ResponseSpecFactory.ok())
                .extract().response();
    }

    public Response authenticateSubscriber(String payload) {
        return given().spec(RequestSpecFactory.getRequestSpec("subscriber"))
                .body(payload)
                .when().post("/authenticate-subscriber")
                .then().spec(ResponseSpecFactory.ok())
                .extract().response();
    }

    public Response getSubscriberShortInfo(String payload) {
        return given().spec(RequestSpecFactory.getRequestSpec("subscriber"))
                .body(payload)
                .when().post("/user/get-subscriber-short-info")
                .then().spec(ResponseSpecFactory.ok())
                .extract().response();
    }

    public Response verifyUserPin(String payload) {
        return given().spec(RequestSpecFactory.getRequestSpec("subscriber"))
                .body(payload)
                .when().post("/verify-user-pin")
                .then().spec(ResponseSpecFactory.ok())
                .extract().response();
    }
    public Response updateNotificationPreference(String payload) {
        return given().spec(RequestSpecFactory.getRequestSpec("subscriber"))
                .body(payload)
                .when().post("/user/notification-preference")
                .then().spec(ResponseSpecFactory.ok())
                .extract().response();
    }

    public Response getNotificationPreference() {
        return given().spec(RequestSpecFactory.getRequestSpec("subscriber"))
                .when().get("/user/notification-preference?userId=41&userType=SUBSCRIBER")
                .then().spec(ResponseSpecFactory.ok())
                .extract().response();
    }
}
