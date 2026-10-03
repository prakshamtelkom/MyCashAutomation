 package com.mycash.core.reporting;

import com.mycash.core.model.ApiResult;
import io.qameta.allure.Allure;
import io.restassured.response.Response;

public final class AllureAttachmentHelper {

    private AllureAttachmentHelper() {}

    private static boolean isDuplicate(String label) {
        return Allure.getLifecycle().getCurrentTestCaseOrStep().isEmpty();
    }

    public static void attachMethod(String method) {
        if (method == null || method.isBlank() || isDuplicate("HTTP Method")) return;
        Allure.addAttachment("HTTP Method", method);
    }

    public static void attachEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isBlank() || isDuplicate("Endpoint")) return;
        Allure.addAttachment("Endpoint", endpoint);
    }

    public static void attachRequestBody(String body) {
        if (body == null || body.isBlank() || isDuplicate("Request Body")) return;
        Allure.addAttachment("Request Body", "application/json", body, ".json");
    }

    public static void attachResponseBody(Response response) {
        if (response == null || response.getBody() == null || isDuplicate("Response Body")) return;
        Allure.addAttachment("Response Body", "application/json", response.getBody().asPrettyString(), ".json");
    }

    public static void attachExecutionSummary(ApiResult result) {
        if (result == null || isDuplicate("Execution Summary")) return;

        StringBuilder summary = new StringBuilder();
        summary.append("API Name      : ").append(result.getApiName()).append("\n")
                .append("Source        : ").append(result.getSource()).append("\n")
                .append("Method        : ").append(result.getMethod()).append("\n")
                .append("Endpoint      : ").append(result.getEndpoint()).append("\n")
                .append("Status Code   : ").append(result.getStatusCode()).append("\n")
                .append("Response Time : ").append(result.getResponseTimeMs()).append(" ms\n")
                .append("Result        : ").append(result.isSuccess() ? "PASS" : "FAIL");

        if (result.getError() != null && !result.getError().isBlank()) {
            summary.append("\nError         : ").append(result.getError());
        }

        Allure.addAttachment("Execution Summary", summary.toString());
    }
}