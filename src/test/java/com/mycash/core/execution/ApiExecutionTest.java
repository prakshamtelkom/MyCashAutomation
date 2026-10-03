package com.mycash.core.execution;

import com.mycash.core.Utills.APIData;
import com.mycash.core.Utills.ApiTestRunner;
import com.mycash.core.model.ApiResult;
import io.qameta.allure.Allure;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApiExecutionTest {

    private final ExecutionContext context;

    public ApiExecutionTest(ExecutionContext context) {
        this.context = context;
    }

    @Test
    public void execute() {

        APIData api = context.getApiData();

        Map<String, APIData> singleApi = new LinkedHashMap<>();
        singleApi.put(api.getApiName(), api);

        ApiResult result =
                ApiTestRunner.runAllApis(
                                context.getWorkbook(),
                                context.getSheetName(),
                                singleApi)
                        .get(api.getApiName());

        addReport(result);

        Assert.assertTrue(
                result.isSuccess(),
                "API FAILED : "
                        + result.getApiName()
                        + "\nStatus : "
                        + result.getStatusCode()
                        + "\nError : "
                        + result.getError());
    }

    private void addReport(ApiResult result) {

        Allure.label("parentSuite", context.getWorkbook());
        Allure.label("suite", context.getSheetName());
        Allure.label("subSuite", context.getApiName());

        Allure.addAttachment(
                "Execution Summary",
                "Source : " + context.getSource()
                        + "\nWorkbook : " + context.getWorkbook()
                        + "\nSheet : " + context.getSheetName()
                        + "\nAPI : " + context.getApiName()
                        + "\nStatus : " + result.getStatusCode()
        );

        if (result.getBody() != null) {

            Allure.addAttachment(
                    "Response Body",
                    "application/json",
                    result.getBody(),
                    ".json");
        }
    }
}