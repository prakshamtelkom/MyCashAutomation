//
//package com.mycash.smoke;
//
//import com.mycash.services.UserService;
//import io.restassured.response.Response;
//import org.testng.Assert;
//import org.testng.annotations.Test;
//
//public class UserSmokeTest {
//private final UserService userService=new UserService();
//    @Test(groups = {"smoke"})
//    public void verifyUsersAPI() {
//
//        Response response = userService.getAllUsers();
//
//        Assert.assertEquals(response.getStatusCode(), 200);
//
//        Assert.assertTrue(response.time() < 3000,
//                "Response time exceeded threshold");
//    }
//}
