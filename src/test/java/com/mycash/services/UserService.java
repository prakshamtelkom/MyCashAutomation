
package com.mycash.services;

import com.mycash.core.clients.UserClient;
import io.restassured.response.Response;

public class UserService {

    private final UserClient userClient = new UserClient();

    public Response getAllUsers(String username) {

        return userClient.getUsers();
    }
    //create user
    public Response createUser(String userKey,String payLoad) {
        return userClient.createUSer(userKey,payLoad);
    }
}
