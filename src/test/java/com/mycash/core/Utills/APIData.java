package com.mycash.core.Utills;

public class APIData {

    private final String apiName;
    private final String endPoint;
    private final String method;
    private final String requestBody;
    private final String responseBody;

    public APIData(String apiName, String endPoint, String method, String requestBody, String responseBody) {
        this.apiName = apiName;
        this.endPoint = endPoint;
        this.method = method;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
    }

    public String getApiName() { return apiName; }
    public String getEndPoint() { return endPoint; }
    public String getMethod() { return method; }
    public String getRequestBody() { return requestBody; }
    public String getResponseBody() { return responseBody; }
}