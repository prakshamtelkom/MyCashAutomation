package com.mycash.core.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.ResponseSpecification;

public final class ResponseSpecFactory {

    private ResponseSpecFactory() {}

    public static ResponseSpecification ok() {
        return build(200);
    }

    public static ResponseSpecification created() {
        return build(201);
    }

    public static ResponseSpecification noContent() {
        return build(204);
    }

    public static ResponseSpecification build(int expectedStatus) {
        return new ResponseSpecBuilder()
                .expectStatusCode(expectedStatus)
                .expectContentType(ContentType.JSON)
                .build();
    }
}
