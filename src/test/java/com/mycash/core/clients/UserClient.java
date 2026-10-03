
package com.mycash.core.clients;

import com.mycash.core.specs.RequestSpecFactory;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class UserClient {


    public Response getUsers() {
        return given().spec(RequestSpecFactory.getRequestSpec()).
                when().get("/users").then().extract().response();
    }

    public Response createUSer(String userKey, String payLoad)
    {
   return given().spec(RequestSpecFactory.getRequestSpec()).when()
           .post("/users").then().extract().response();
    }
}
