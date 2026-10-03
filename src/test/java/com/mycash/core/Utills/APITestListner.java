package com.mycash.core.Utills;

import io.restassured.response.Response;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class APITestListner implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {

        Object[] params = result.getParameters();

        if (params == null || params.length == 0) {
            return;
        }

        for (Object param : params) {

            // ================= REST ASSURED FAILURE =================
            if (param instanceof Response response) {

                ApiResponseLogger.logApiResponse(response);
            }

            // ================= PERFORMANCE TEST PLACEHOLDER =================

            else if (param != null &&
                    param.getClass().getSimpleName().equals("TestPlanStats")) {

                try {
                    // reflection-safe handling (avoids compile issues)
                    var overallMethod = param.getClass().getMethod("overall");
                    Object overall = overallMethod.invoke(param);

                    var sampleTimeMethod = overall.getClass().getMethod("sampleTime");
                    Object sampleTime = sampleTimeMethod.invoke(overall);

                    double max = (double) sampleTime.getClass().getMethod("max").invoke(sampleTime);
                    double min = (double) sampleTime.getClass().getMethod("min").invoke(sampleTime);
                    double mean = (double) sampleTime.getClass().getMethod("mean").invoke(sampleTime);

                    ApiResponseLogger.logPerformanceMetrics(max, min, mean);

                } catch (Exception e) {
                    System.out.println("Failed to extract TestPlanStats: " + e.getMessage());
                }
            }
        }
    }
}