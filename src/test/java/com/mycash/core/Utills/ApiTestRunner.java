package com.mycash.core.Utills;

import com.mycash.core.config.ConfigReader;
import com.mycash.core.model.ApiResult;
import com.mycash.core.specs.RequestSpecFactory;

import io.restassured.response.Response;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import io.qameta.allure.Allure;
import io.restassured.specification.RequestSpecification;
import io.qameta.allure.Allure;
import io.restassured.specification.RequestSpecification;

import java.util.concurrent.atomic.AtomicInteger;
import static io.restassured.RestAssured.given;
import com.mycash.core.reporting.ExtentReportLogger;

public class ApiTestRunner {
    private static final AtomicInteger executionCounter = new AtomicInteger();

    private static int passed = 0;
    private static int failed = 0;

    private static void attachRequest(APIData data, String endpoint) {

        Allure.addAttachment(
                "HTTP Method",
                data.getMethod()
        );

        Allure.addAttachment(
                "Endpoint",
                endpoint
        );

        String requestBody =
                data.getRequestBody() == null
                        ? "{}"
                        : data.getRequestBody();

        Allure.addAttachment(
                "Request Body",
                "application/json",
                requestBody,
                ".json"
        );
    }

    private static void attachResponse(Response response) {

        if (response != null) {

            Allure.addAttachment(
                    "Status Code",
                    String.valueOf(response.getStatusCode())
            );

            Allure.addAttachment(
                    "Response Body",
                    "application/json",
                    response.getBody().asPrettyString(),
                    ".json"
            );
        }
    }

    private static void attachError(Exception e) {

        Allure.addAttachment(
                "Exception",
                e.getMessage()
        );
    }

    public static Map<String, ApiResult> runAllApis(
            String workbookName,
            String sheetName,
            Map<String, APIData> apiMap) {


        Map<String, ApiResult> results =
                new LinkedHashMap<>();


        System.out.println();
        System.out.println("=================================================");
        System.out.println("WORKBOOK : " + workbookName);
        System.out.println("SHEET    : " + sheetName);
        System.out.println("TOTAL API: " + apiMap.size());
        System.out.println("=================================================");

        executionCounter.set(0);
        passed = 0;
        failed = 0;
        int totalApis = apiMap.size();

        for (Map.Entry<String, APIData> entry : apiMap.entrySet()) {

            APIData data = entry.getValue();

            int current = executionCounter.incrementAndGet();

            System.out.println();
            System.out.println("=================================================");
            System.out.println("[" + current + "/" + totalApis + "] Executing API");
            System.out.println("API      : " + data.getApiName());
            System.out.println("METHOD   : " + data.getMethod());
            System.out.println("ENDPOINT : " + data.getEndPoint());
            System.out.println("=================================================");

            ApiResult result;

            try {

                result = executeRequest(data);

            } catch (Exception e) {

                result = new ApiResult(
                        data.getApiName(),
                        e.getMessage()
                );

            }

            if (result.isSuccess()) {
                passed++;
            } else {
                failed++;
            }

            printConsoleResult(data, result);
            /*
             * Extent Reporting
             */
            ExtentReportLogger.logApiExecution(
                    workbookName,
                    sheetName,
                    result
            );
            results.put(
                    data.getApiName(),
                    result
            );
        }System.out.println();
        System.out.println("=================================================");
        System.out.println("EXECUTION SUMMARY");
        System.out.println("=================================================");
        System.out.println("Total APIs Executed : " + executionCounter.get());
        System.out.println("Passed             : " + passed);
        System.out.println("Failed             : " + failed);
        System.out.println("=================================================");
        System.out.println();

        return results;

    }




    private static ApiResult executeRequest(
            APIData data) {


        String endpoint =
                normalizeEndpoint(
                        data.getEndPoint()
                );
        //RequestSpecification spec = RequestSpecFactory.getRequestSpec();

        String baseUrl = ConfigReader.get("baseUrl");

        String requestUri = baseUrl.endsWith("/") && endpoint.startsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1) + endpoint
                : baseUrl + endpoint;
        Allure.addAttachment("HTTP Method", data.getMethod());
        Allure.addAttachment("Request URI", requestUri);


        long startTime =
                System.currentTimeMillis();



        try {


            Response response;



            switch (
                    data.getMethod()
                            .trim()
                            .toUpperCase()
            ) {


                case "GET":
                    attachRequest(data, endpoint);
                    response =
                            given()
                                    .spec(
                                            RequestSpecFactory
                                                    .getRequestSpec()
                                    )
                                    .relaxedHTTPSValidation()
                                    .log()
                                    .all()
                                    .when()
                                    .get(endpoint);

                    break;



                case "POST":
                    attachRequest(data, endpoint);
                    response =
                            given()
                                    .spec(
                                            RequestSpecFactory
                                                    .getRequestSpec()
                                    )
                                    .relaxedHTTPSValidation()
                                    .header(
                                            "Content-Type",
                                            "application/json"
                                    )
                                    .body(
                                            data.getRequestBody() == null
                                                    ?
                                                    "{}"
                                                    :
                                                    data.getRequestBody()
                                    )
                                    .log()
                                    .all()
                                    .when()
                                    .post(endpoint);

                    break;



                case "PUT":
                    attachRequest(data, endpoint);
                    response =
                            given()
                                    .spec(
                                            RequestSpecFactory
                                                    .getRequestSpec()
                                    )
                                    .relaxedHTTPSValidation()
                                    .header(
                                            "Content-Type",
                                            "application/json"
                                    )
                                    .body(
                                            data.getRequestBody() == null
                                                    ?
                                                    "{}"
                                                    :
                                                    data.getRequestBody()
                                    )
                                    .log()
                                    .all()
                                    .when()
                                    .put(endpoint);

                    break;



                case "DELETE":
                    attachRequest(data, endpoint);
                    response =
                            given()
                                    .spec(
                                            RequestSpecFactory
                                                    .getRequestSpec()
                                    )
                                    .relaxedHTTPSValidation()
                                    .log()
                                    .all()
                                    .when()
                                    .delete(endpoint);

                    break;



                default:
                    attachRequest(data, endpoint);
                    throw new IllegalArgumentException(
                            "Unsupported HTTP Method : "
                                    + data.getMethod()
                    );

            }



            long responseTime =
                    System.currentTimeMillis()
                            - startTime;
            attachResponse(response);


            return new ApiResult(
                    data.getApiName(),
                    "Excel",
                    endpoint,
                    data.getMethod(),
                    data.getRequestBody(),
                    response,
                    responseTime,
                    null
            );



        } catch(Exception e) {
            attachError(e);

            long responseTime =
                    System.currentTimeMillis()
                            - startTime;



            return new ApiResult(
                    data.getApiName(),
                    "Excel",
                    endpoint,
                    data.getMethod(),
                    data.getRequestBody(),
                    null,
                    responseTime,
                    e.getMessage()
            );
        }

    }





    private static String normalizeEndpoint(String endpoint) {

        if (endpoint == null || endpoint.trim().isEmpty()) {
            return "";
        }

        endpoint = endpoint.trim();

        try {

            if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {

                URI uri = URI.create(endpoint);

                StringBuilder normalized = new StringBuilder();

                if (uri.getPath() != null) {
                    normalized.append(uri.getPath());
                }

                if (uri.getQuery() != null && !uri.getQuery().isEmpty()) {
                    normalized.append("?").append(uri.getQuery());
                }

                endpoint = normalized.toString();
            }

        } catch (Exception e) {

            System.out.println("Invalid endpoint: " + endpoint);
        }

        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }

        return endpoint;
    }




    private static void printConsoleResult(
            APIData data,
            ApiResult result) {



        System.out.println(
                "--------------------------------------"
        );


        System.out.println(
                "API          : "
                        + data.getApiName()
        );


        System.out.println(
                "METHOD       : "
                        + data.getMethod()
        );


        System.out.println(
                "ENDPOINT     : "
                        + data.getEndPoint()
        );


        System.out.println(
                "STATUS       : "
                        + result.getStatusCode()
        );


        System.out.println(
                "RESULT       : "
                        +
                        (
                                result.isSuccess()
                                        ?
                                        "PASS"
                                        :
                                        "FAIL"
                        )
        );


        if(result.getError()!=null) {


            System.out.println(
                    "ERROR        : "
                            + result.getError()
            );

        }


        System.out.println(
                "--------------------------------------"
        );


    }


}