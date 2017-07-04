package com.toptal.calorie.test.resource;

import com.toptal.calorie.constant.Constants;
import com.toptal.calorie.util.HttpUtil;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by asirna on 03/07/2017.
 * Do CRUD on Users as user_manager user
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserManagerUserResourceTest {

    private final String baseUrl = "http://localhost:8080/api";
    private final String loginUrl = baseUrl + "/auth/login";
    private final String usersUrl = baseUrl + "/users";
    private static String userId = null;
    private static String name = "UserResourceTest";
    private static String email = null;
    private static String userManagerEmail = "user_manager@gmail.com";
    private static String role = "user";
    private static String password = "password";
    private static String token = null;

    @BeforeClass
    public static void setUp() {
        email = "UserResourceTest" + System.currentTimeMillis() + "@gmail.com";
    }

    @Test
    public void a_testCreateUser() throws Exception {
        JSONObject body = new JSONObject();
        body.put(Constants.NAME, name);
        body.put(Constants.EMAIL, email);
        body.put(Constants.ROLE, role);
        body.put(Constants.PASSWORD, password);
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.post(usersUrl, headers, body);
        userId = resp.getString("userId");
        System.out.println("userId is : " + userId);
        Assert.assertEquals(email, resp.getString(Constants.EMAIL));
        Assert.assertEquals(name, resp.getString(Constants.NAME));
        Assert.assertEquals(role, resp.getString(Constants.ROLE));
    }

    @Test
    public void b_login() throws Exception {
        JSONObject body = new JSONObject();
        body.put(Constants.EMAIL, userManagerEmail);
        body.put(Constants.PASSWORD, password);
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.post(loginUrl, headers, body);
        token = resp.getString(Constants.TOKEN);
        Assert.assertEquals(userManagerEmail, resp.getJSONObject(Constants.PROFILE).getString(Constants.EMAIL));
        Assert.assertEquals("user_manager", resp.getJSONObject(Constants.PROFILE).getString(Constants.ROLE));
    }

    @Test
    public void c_getUserById() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.get(usersUrl + "/" + userId, headers);
        Assert.assertEquals(email, resp.getString(Constants.EMAIL));
        Assert.assertEquals(name, resp.getString(Constants.NAME));
        Assert.assertEquals(role, resp.getString(Constants.ROLE));
        Assert.assertEquals(userId, resp.getString("userId"));
    }

    @Test
    public void d_updateUser() throws Exception {
        JSONObject body = new JSONObject();
        String newName = name + "xz";
        double targetCalories = 60.34;
        body.put(Constants.EMAIL, email);
        body.put(Constants.NAME, newName);
        body.put(Constants.TARGET_CALORIES, targetCalories);
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.put(usersUrl + "/" + userId, headers, body);
        System.out.println("userId is : " + userId);
        Assert.assertEquals(email, resp.getString(Constants.EMAIL));
        Assert.assertEquals(newName, resp.getString(Constants.NAME));
        Assert.assertEquals(role, resp.getString(Constants.ROLE));
        Assert.assertEquals(userId, resp.getString("userId"));
        Assert.assertEquals(targetCalories, resp.getDouble(Constants.TARGET_CALORIES),0);
    }

    @Test
    public void e_deleteUser() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.delete(usersUrl + "/" + userId, headers);
        Assert.assertEquals(resp.toString(),"{}");
        resp = util.get(usersUrl + "/" + userId, headers);
        Assert.assertEquals(false, resp.getBoolean("success"));
        Assert.assertEquals("Failed to fetch user info", resp.getString("message"));
    }
}
