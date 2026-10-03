package com.mycash.core.Utills;


import io.qameta.allure.Allure;
import io.restassured.response.Response;

import java.time.Duration;

public class ApiResponseLogger {
    public static void logApiResponse(Response response) {

        //log status code
        Allure.step("Status Code: " + response.getStatusCode());

        //log response time
        Allure.step("ResponseTime: " + response.getTime());

        //attach responsebody
       //Allure.addAttachment("ResponseBody", response.getBody().asString());

    }
    //log request
    public static void logAPIRequest(String endpoint ,String Payload) {
       Allure.addAttachment("Request endpoint", endpoint);
      // Allure.addAttachment("Payload","application/json", Payload);
    }

    public static void logPerformanceMetrics(double slow,double fast,double avg){
       String metrics="Avg response time"
+ avg +"ms\n" +"Slow response time"+slow+"ms\n" +"Fast response time"+fast+"ms";
       Allure.addAttachment("Metrics",metrics);
    }

}
