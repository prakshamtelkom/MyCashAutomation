//package com.mycash.api.tests;
//
//import com.mycash.core.Utills.TestDataLoader;
//import com.mycash.core.clients.AuthenticationClient;
//import io.restassured.response.Response;
//import org.testng.Assert;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
//import java.util.Map;
//
//public class AuthenticationAndUserFlowTests {
//    private AuthenticationClient authClient;
//    private Map<String, Object> testData;
//    private String token;
//
//    @BeforeClass
//    public void setup() {
//        authClient = new AuthenticationClient();
//        // Load payloads from YAML file
//        TestDataLoader.load("environments/Mycash_final.postman_collection.json");
//    }
//
//    //  Authentication Flow
//    @Test
//    public void testRequestOtp() {
//        Response response = authClient.requestOtp(testData.get("requestOtp").toString());
//        Assert.assertEquals(response.statusCode(), 200);
//    }
//
//    @Test public void testGenerateToken() {
//        Response response = authClient.generateToken(testData.get("generateToken").toString());
//        token = response.jsonPath().getString("token");
//        Assert.assertNotNull(token);
//    }
//
//    @Test public void testAuthenticateSubscriber() {
//        Response response = authClient.authenticateSubscriber(testData.get("authenticateSubscriber").toString());
//        Assert.assertEquals(response.statusCode(), 200);
//    }
//
//    // User APIs
//    @Test public void testGetSubscriberShortInfo() {
//        Response response = authClient.getSubscriberShortInfo(testData.get("getSubscriberShortInfo").toString());
//        Assert.assertEquals(response.statusCode(), 200);
//    }
//
//    @Test public void testVerifyUserPin() {
//        Response response = authClient.verifyUserPin(testData.get("verifyUserPin").toString());
//        Assert.assertEquals(response.statusCode(), 200);
//    }
//
//    @Test public void testGetNotificationPreference() {
//        Response response = authClient.getNotificationPreference();
//        Assert.assertEquals(response.statusCode(), 200);
//    }
//
//    @Test public void testUpdateNotificationPreference() {
//        Response response = authClient.updateNotificationPreference(testData.get("updateNotificationPreference").toString());
//        Assert.assertEquals(response.statusCode(), 200);
//    }
//
//    //  Registration APIs
//    @Test public void testGetDomainsCountries() {
//        /* call /self/domains */ }
//    @Test public void testGetDomainAreasCities() {
//        /* call /self/domain/areas */ }
//    @Test public void testSubscriberRegistrationStep1() {
//        /* call /self/subscriber-registration/step-1 */ }
//    @Test public void testRunBeforeStep1Otp() {
//        /* call /self/subscriber-registration/otp */ }
//    @Test public void testGetDomainsZones() {
//        /* call /self/domain/areas?id=... */ }
//
//    //  Transfer & Payment APIs
//    @Test public void testUserMiniStatement() {
//        /* call /transfer/user-mini-statement */ }
//    @Test public void testBillerTransactionList() {
//        /* call /transfer/biller-transaction-list */ }
//    @Test public void testSingleSubscriberReport() {
//        /* call /transaction-report/single-subscriber-report-mobile */ }
//    @Test public void testGetTransferDetail() {
//        /* call /transfer/get-transfer-detail */ }
//    @Test public void testP2PTransfer() {
//        /* call /transfer/p2p-transfer */ }
//    @Test public void testTopupAirtimeData() {
//        /* call /transfer/topup */ }
//    @Test public void testPayBill() {
//        /* call /transfer/pay-bill */ }
//
//    //  Amal Bank APIs
//    @Test public void testGetAmalBankAccounts() {
//        /* call /transfer/amal-bank/{walletId} */ }
//    @Test public void testGetBeneficiariesByUserId() {
//        /* call /user/beneficiaries-by-userId/{userId} */ }
//    @Test public void testGetBankStatement() {
//        /* call /amal-bank/get-statement */ }
//
//    //  Amal Express Remittance APIs
//    @Test public void testRemittanceListByMobile() {
//        /* call /transfer/remittance-list-by-mobile */ }
//    @Test public void testGetCountries() {
//        /* call /api/amal-express/country */ }
//    @Test public void testGetCitiesByCountry() {
//        /* call /api/amal-express/GetCitiesByCountryId */ }
//    @Test public void testGetServices() {
//        /* call /api/amal-express/getServices */ }
//    @Test public void testGetServiceOperators() {
//        /* call /api/amal-express/getServicesOperators */ }
//    @Test public void testRemittanceTransactionList() {
//        /* call /transfer/remittance-transaction-list */ }
//    @Test public void testRemittanceTransactionDetails() {
//        /* call /transfer/remittance-transaction-details */ }
//    @Test public void testGetBranches() {
//        /* call /api/amal-express/branches */ }
//    @Test public void testSearchRemitter() {
//        /* call /api/amal-express/search-remitter */ }
//
//    //  Common & Support
//    @Test public void testGetDataBundles() {
//        /* call /data-bundle/get-grouped-data-bundles */ }
//    @Test public void testGetDueBill() {
//        /* call /biller/api/get-due-bill */ }
//    @Test public void testFaqList() {
//        /* call /faq/list */ }
//}
