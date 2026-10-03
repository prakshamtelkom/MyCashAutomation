package com.mycash.core.model;

import io.restassured.response.Response;

public class ApiResult {

    private final String apiName;
    private final String source;
    private final String endpoint;
    private final String method;
    private final String requestBody;

    private final Response response;

    private final int statusCode;
    private final String body;
    private final long responseTimeMs;
    private final String error;
    private final boolean success;

    /*
     * Used by ApiUtil
     */
    public ApiResult(String apiName, Response response) {

        this(
                apiName,
                "Unknown",
                "",
                "",
                "",
                response,
                0,
                null
        );

    }

    /*
     * Used when exception occurs
     */
    public ApiResult(String apiName, String error) {

        this(
                apiName,
                "Unknown",
                "",
                "",
                "",
                null,
                0,
                error
        );

    }

    /*
     * Used by PostmanCollectionRunner
     */
    public ApiResult(
            String apiName,
            String source,
            String endpoint,
            String method,
            Response response,
            long responseTime,
            String error) {

        this(
                apiName,
                source,
                endpoint,
                method,
                "",
                response,
                responseTime,
                error
        );

    }

    /*
     * Used by ApiTestRunner
     */
    public ApiResult(
            String apiName,
            String source,
            String endpoint,
            String method,
            String requestBody,
            Response response,
            long responseTime,
            String error) {

        this.apiName = apiName;
        this.source = source;
        this.endpoint = endpoint;
        this.method = method;
        this.requestBody = requestBody;
        this.response = response;
        this.responseTimeMs = responseTime;
        this.error = error;

        if (response != null) {

            this.statusCode = response.getStatusCode();

            String tmp;

            try {
                tmp = response.getBody().asPrettyString();
            } catch (Exception e) {
                tmp = "";
            }

            this.body = tmp;

        } else {

            this.statusCode = 0;
            this.body = "";

        }

        this.success =
                response != null &&
                        response.getStatusCode() >= 200 &&
                        response.getStatusCode() < 300 &&
                        error == null;

    }

    public String getApiName() {
        return apiName;
    }

    public String getSource() {
        return source;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getMethod() {
        return method;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public Response getResponse() {
        return response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public String getError() {
        return error;
    }

    public boolean isSuccess() {
        return success;
    }
    public String getStatus(){
        return isSuccess()?"success":"failure";
    }

}