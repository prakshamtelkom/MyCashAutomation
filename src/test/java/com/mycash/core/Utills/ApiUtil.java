package com.mycash.core.Utills;

import com.mycash.core.model.ApiResult;
import com.mycash.core.specs.RequestSpecFactory;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ApiUtil {

    public static ApiResult executeApi(String apiName, String method, String url) {

        Response response;

        switch (method.toUpperCase()) {

            case "GET":
                response = given()
                        .spec(RequestSpecFactory.getRequestSpec())
                        .log().all()
                        .when()
                        .get(url);
                break;

            case "POST":
                response = given()
                        .spec(RequestSpecFactory.getRequestSpec())
                        .body("{}")
                        .log().all()
                        .when()
                        .post(url);
                break;

            case "PUT":
                response = given()
                        .spec(RequestSpecFactory.getRequestSpec())
                        .body("{}")
                        .when()
                        .put(url);
                break;

            case "DELETE":
                response = given()
                        .spec(RequestSpecFactory.getRequestSpec())
                        .when()
                        .delete(url);
                break;

            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
        }

        return new ApiResult(apiName, response);
    }
}