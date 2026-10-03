package com.mycash.api.tests;

import com.mycash.core.Utills.APIData;
import com.mycash.core.Utills.ApiTestRunner;
import com.mycash.core.Utills.ExcelUtils;
import com.mycash.core.Utills.PostmanCollectionRunner;
import com.mycash.core.config.ConfigReader;
import com.mycash.core.execution.ExecutionFilter;
import com.mycash.core.model.ApiResult;
import com.mycash.core.reporting.AllureAttachmentHelper;
import com.mycash.core.reporting.ExtentManager;
import com.mycash.core.reporting.ExtentReportLogger;
import com.mycash.core.reporting.ReportNodeManager;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.testng.AllureTestNg;

import org.testng.Assert;
import org.testng.annotations.*;

import java.util.*;

@Listeners(AllureTestNg.class)
@Epic("MyCash API Automation")
public class RunFullCollectionTest {

    private static final String POSTMAN_COLLECTION =
           "environments/Mycash_final.postman_collection1.json";

    private static final String[] EXCEL_FILES = {

            //no auth
//            "src/main/resources/20260906_9APIsCore8080.xlsx",
//            "src/main/resources/New1_9APIsCore 8080.xlsx",
//            "src/main/resources/1_72APIsCore8080.xlsx",
           // "src/main/resources/20260917_25APIsGatewayCore8080.xlsx",

//"src/main/resources/ConsolidatedPIverification.xlsx"
            //auth require group
            "src/main/resources/MyCash_APIs_3088.xlsx"
            //"src/main/resources/20260917_20APIsGateway3088.xlsx",
//            "src/main/resources/20260915_51APIsGateway3088.xlsx",
//            "src/main/resources/20260913111APIsGateway3088.xlsx",
//
//            "src/main/resources/20260908AmalExpressAPIs.xlsx",
//            "src/main/resources/20260908AmalBankAPIs.xlsx",
//            "src/main/resources/New1_91APIsGateway3088.xlsx",
//            "src/main/resources/1_108APIsGateway3088.xlsx",
//            "src/main/resources/20260906_15APIsGateway3088.xlsx",
//            "src/main/resources/2026910_101APIsGateway3088.xlsx",
//            "src/main/resources/AmalExpressGateway3088.xlsx"
            // "src/main/resources/AmalExpressCore8080.xlsx"
           // "src/main/resources/2026_09_06_1APIsGateway3088.xlsx"
            //"src/main/resources/New1_91APIsGateway3088.xlsx"
            //"src/main/resources/1_72_APIsCore_8080.xlsx"
           // "src/main/1_80_APIs_API_blocker.xlsx"
           // "src/main/resources/1_72APIsCore8080.xlsx"
           // "src/main/resources/1_108APIsGateway3088.xlsx"
            //"src/main/resources/Clean_Fintech_Banking_APIs_2.xlsx",
            //"src/main/resources/Clean_Fintech_Banking_Wallet_API_Details.xlsx"
           // "src/main/resources/New1_91APIsGateway3088.xlsx"
    };

    private final ExecutionFilter executionFilter =
            new ExecutionFilter();

    @BeforeClass
    public void setup() {

        ConfigReader.load("qa.properties");

        System.out.println("API Automation Started");
        System.out.println(executionFilter);
    }

    @AfterSuite(alwaysRun = true)
    public void tearDown() {

        ExtentManager.flush();
        ReportNodeManager.clear();
    }

    @DataProvider(name = "allApis", parallel = true)
    public Object[][] allApis() throws Exception {

        List<Object[]> data = new ArrayList<>();

        loadExcelApis(data);
        loadPostmanApis(data);

        return data.toArray(new Object[0][]);
    }

    /**
     * Loads Excel APIs based on ExecutionFilter.
     *
     * Supported filters:
     *
     * -Dworkbook=<workbook>
     * -Dsheet=<sheet>
     * -Dapi=<api>
     * -Dmethod=<method>
     * -Dsource=<source>
     * -Dtags=<tags>
     */
    private void loadExcelApis(List<Object[]> data)
            throws Exception {

        for (String workbook : EXCEL_FILES) {

            /*
             * Workbook-level filtering
             */
            if (!executionFilter.shouldExecuteWorkbook(workbook)) {

                System.out.println(
                        "Skipping workbook: " + workbook
                );

                continue;
            }

            Map<String, Map<String, APIData>> sheets =
                    ExcelUtils.getExcelDataBySheet(workbook);

            for (Map.Entry<String, Map<String, APIData>> sheetEntry :
                    sheets.entrySet()) {

                String sheetName = sheetEntry.getKey();

                /*
                 * Sheet-level filtering
                 */
                if (!executionFilter.shouldExecuteSheet(sheetName)) {

                    System.out.println(
                            "Skipping sheet: " + sheetName +
                                    " from workbook: " + workbook
                    );

                    continue;
                }

                for (APIData api :
                        sheetEntry.getValue().values()) {

                    /*
                     * API-level filtering
                     */
                    if (!executionFilter.shouldExecuteApi(
                            api.getApiName())) {

                        continue;
                    }

                    /*
                     * Method-level filtering
                     */
                    if (!executionFilter.shouldExecuteMethod(
                            api.getMethod())) {

                        continue;
                    }

                    data.add(
                            new Object[]{
                                    "Excel",
                                    workbook,
                                    sheetName,
                                    api
                            }
                    );
                }
            }
        }
    }

    /**
     * Loads and executes the configured Postman collection.
     *
     * Postman execution remains independent of Excel filters.
     */
    private void loadPostmanApis(List<Object[]> data)
            throws Exception {

        PostmanCollectionRunner runner =
                new PostmanCollectionRunner(
                        POSTMAN_COLLECTION
                );

        Map<String, ApiResult> results =
                runner.runAll();

        for (ApiResult result : results.values()) {

            data.add(
                    new Object[]{
                            "Postman",
                            POSTMAN_COLLECTION,
                            "Postman Collection",
                            result
                    }
            );
        }
    }

    @Test(
            dataProvider = "allApis",
            groups = "api"
    )
    @Feature("API Execution")
    public void executeApi(
            String source,
            String file,
            String location,
            Object apiObject) {

        ApiResult result =
                executeApi(
                        file,
                        location,
                        apiObject
                );

        /*
         * Dynamic test case naming for Allure
         */
        Allure.getLifecycle().updateTestCase(tc -> {

            tc.setName(
                    result.getApiName()
            );

            tc.setFullName(
                    source +
                            " | " +
                            location +
                            " | " +
                            result.getApiName()
            );
        });

        /*
         * Suite hierarchy labels
         */
        Allure.label(
                "parentSuite",
                "MyCash APIs"
        );

        Allure.label(
                "suite",
                location
        );

        Allure.label(
                "subSuite",
                source
        );

        Allure.label(
                "epic",
                source
        );

        Allure.label(
                "feature",
                location
        );

        Allure.label(
                "story",
                result.getApiName()
        );

        /*
         * Step summary
         */
        Allure.step(
                result.getApiName() +
                        " - " +
                        result.getStatus()
        );

        /*
         * Allure attachments
         */
        AllureAttachmentHelper.attachExecutionSummary(
                result
        );

        AllureAttachmentHelper.attachMethod(
                result.getMethod()
        );

        AllureAttachmentHelper.attachEndpoint(
                result.getEndpoint()
        );

        AllureAttachmentHelper.attachRequestBody(
                result.getRequestBody()
        );

        if (result.getBody() != null) {

            Allure.addAttachment(
                    "Response Body",
                    "application/json",
                    result.getBody()
            );
        }

        /*
         * Extent reporting
         */
        ExtentReportLogger.logApiExecution(
                file,
                location,
                result
        );

        /*
         * Final validation
         */
        validateResult(result);
    }

    /**
     * Executes either an Excel API or returns
     * the already executed Postman result.
     */
    private ApiResult executeApi(
            String file,
            String location,
            Object apiObject) {

        if (apiObject instanceof APIData) {

            APIData api =
                    (APIData) apiObject;

            Map<String, APIData> singleApi =
                    new LinkedHashMap<>();

            singleApi.put(
                    api.getApiName(),
                    api
            );

            Map<String, ApiResult> results =
                    ApiTestRunner.runAllApis(
                            file,
                            location,
                            singleApi
                    );

            return results.get(
                    api.getApiName()
            );
        }

        return (ApiResult) apiObject;
    }

    /**
     * Validates API execution result.
     */
    private void validateResult(ApiResult result) {

        Assert.assertTrue(
                result.isSuccess(),
                "API FAILED : " +
                        result.getApiName() +
                        "\nStatus : " +
                        result.getStatusCode() +
                        "\nError : " +
                        result.getError()
        );
    }
}