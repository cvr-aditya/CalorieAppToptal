package com.toptal.calorie.test.resource;

import com.toptal.calorie.constant.Constants;
import com.toptal.calorie.constant.Role;
import com.toptal.calorie.model.Meal;
import com.toptal.calorie.util.GsonUtil;
import com.toptal.calorie.util.HttpUtil;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runners.MethodSorters;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by asirna on 03/07/2017.
 * Test authorization
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthorizationTest {

    private static final String baseUrl = "http://localhost:8080/api";
    private static final String loginUrl = baseUrl + "/auth/login";
    private static final String usersUrl = baseUrl + "/users";
    private static final String mealsUrl = baseUrl + "/meals";
    private static String userId1 = null;
    private static String email1 = null;
    private static String adminUserId = null;
    private static String userManagerUserId = null;
    private static String userId2 = null;
    private static String email2 = null;
    private static String mealId1 = null;
    private static String mealId2 = null;
    private static final String adminEmail = "admin@gmail.com";
    private static final String userManagerEmail = "user_manager@gmail.com";
    private static String user1Token = null;
    private static String user2Token = null;
    private static String adminToken = null;
    private static String userManagerToken = null;
    private static String name = "UserResourceTest";
    private static String role = "user";
    private static String password = "password";
    private static float calories = 98;

    @BeforeClass
    public static void setUp() throws Exception {

        email1 = "user111" + System.currentTimeMillis() + "@gmail.com";
        email2 = "user222" + System.currentTimeMillis() + "@gmail.com";

        // Create user 1
        JSONObject body = new JSONObject();
        body.put(Constants.NAME, name);
        body.put(Constants.EMAIL, email1);
        body.put(Constants.ROLE, role);
        body.put(Constants.PASSWORD, password);
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.post(usersUrl, headers, body);
        userId1 = resp.getString("userId");

        Assert.assertEquals(email1, resp.getString(Constants.EMAIL));
        Assert.assertEquals(name, resp.getString(Constants.NAME));
        Assert.assertEquals(role, resp.getString(Constants.ROLE));

        // Create user 2
        body = new JSONObject();
        body.put(Constants.NAME, name);
        body.put(Constants.EMAIL, email2);
        body.put(Constants.ROLE, role);
        body.put(Constants.PASSWORD, password);
        util = new HttpUtil();
        resp = util.post(usersUrl, headers, body);
        userId2 = resp.getString("userId");

        Assert.assertEquals(email2, resp.getString(Constants.EMAIL));
        Assert.assertEquals(name, resp.getString(Constants.NAME));
        Assert.assertEquals(role, resp.getString(Constants.ROLE));

        // login with user 1
        body = new JSONObject();
        body.put(Constants.EMAIL, email1);
        body.put(Constants.PASSWORD, password);
        util = new HttpUtil();
        resp = util.post(loginUrl, headers, body);
        user1Token = resp.getString(Constants.TOKEN);
        Assert.assertEquals(email1, resp.getJSONObject(Constants.PROFILE).getString(Constants.EMAIL));
        Assert.assertEquals(name, resp.getJSONObject(Constants.PROFILE).getString(Constants.NAME));
        Assert.assertEquals(role, resp.getJSONObject(Constants.PROFILE).getString(Constants.ROLE));
        Assert.assertEquals(userId1, resp.getJSONObject(Constants.PROFILE).getString("userId"));

        // login with user 2
        body = new JSONObject();
        body.put(Constants.EMAIL, email2);
        body.put(Constants.PASSWORD, password);
        util = new HttpUtil();
        resp = util.post(loginUrl, headers, body);
        user2Token = resp.getString(Constants.TOKEN);
        Assert.assertEquals(email2, resp.getJSONObject(Constants.PROFILE).getString(Constants.EMAIL));
        Assert.assertEquals(name, resp.getJSONObject(Constants.PROFILE).getString(Constants.NAME));
        Assert.assertEquals(role, resp.getJSONObject(Constants.PROFILE).getString(Constants.ROLE));
        Assert.assertEquals(userId2, resp.getJSONObject(Constants.PROFILE).getString("userId"));

        // login as admin
        body = new JSONObject();
        body.put(Constants.EMAIL, adminEmail);
        body.put(Constants.PASSWORD, password);
        util = new HttpUtil();
        resp = util.post(loginUrl, headers, body);
        adminUserId = resp.getJSONObject(Constants.PROFILE).getString("userId");
        adminToken = resp.getString(Constants.TOKEN);
        Assert.assertEquals(adminEmail, resp.getJSONObject(Constants.PROFILE).getString(Constants.EMAIL));
        Assert.assertEquals(Role.ADMIN.getRole(), resp.getJSONObject(Constants.PROFILE).getString(Constants.ROLE));

        // login as user_manager
        body = new JSONObject();
        body.put(Constants.EMAIL, userManagerEmail);
        body.put(Constants.PASSWORD, password);
        util = new HttpUtil();
        resp = util.post(loginUrl, headers, body);
        userManagerToken = resp.getString(Constants.TOKEN);
        userManagerUserId = resp.getJSONObject(Constants.PROFILE).getString("userId");
        Assert.assertEquals(userManagerEmail, resp.getJSONObject(Constants.PROFILE).getString(Constants.EMAIL));
        Assert.assertEquals(Role.USER_MANAGER.getRole(), resp.getJSONObject(Constants.PROFILE).getString(Constants.ROLE));

        //create meal for user 1
        Meal meal = new Meal();
        meal.setItemName(name);
        meal.setCalories(calories);
        meal.setEmail(email1);

        headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, user1Token);
        util = new HttpUtil();
        resp = util.post(mealsUrl, headers, GsonUtil.toJsonObject(meal));
        mealId1 = resp.getString("mealId");
        Assert.assertEquals(name, resp.getString(Constants.ITEM_NAME));
        Assert.assertEquals(calories, resp.getDouble(Constants.CALORIES), 0);
        Assert.assertEquals(email1, resp.getString(Constants.EMAIL));

        //create meal for user 2
        Meal meal2 = new Meal();
        meal2.setItemName(name);
        meal2.setCalories(calories);
        meal2.setEmail(email2);

        headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, user2Token);
        util = new HttpUtil();
        resp = util.post(mealsUrl, headers, GsonUtil.toJsonObject(meal2));
        mealId2 = resp.getString("mealId");
        Assert.assertEquals(name, resp.getString(Constants.ITEM_NAME));
        Assert.assertEquals(calories, resp.getDouble(Constants.CALORIES), 0);
        Assert.assertEquals(email2, resp.getString(Constants.EMAIL));
    }

    @Test
    public void a_testGetUser1DetailsAsUser2() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, user2Token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.get(usersUrl + "/" + userId1, headers);
        Assert.assertFalse(resp.getBoolean(Constants.SUCCESS));
        Assert.assertEquals("User cannot perform the operation", resp.getString("message"));
    }

    @Test
    public void b_testUpdateUser1DetailsAsUser2() throws Exception {
        JSONObject body = new JSONObject();
        String newName = name + "xz";
        double targetCalories = 60.34;
        body.put(Constants.NAME, newName);
        body.put(Constants.TARGET_CALORIES, targetCalories);
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, user2Token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.put(usersUrl + "/" + userId1, headers, body);
        Assert.assertFalse(resp.getBoolean(Constants.SUCCESS));
        Assert.assertEquals("User cannot perform the operation", resp.getString("message"));
    }

    @Test
    public void c_testDeleteUser1DetailsAsUser2() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, user2Token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.delete(usersUrl + "/" + userId1, headers);
        Assert.assertFalse(resp.getBoolean(Constants.SUCCESS));
        Assert.assertEquals("User cannot perform the operation", resp.getString("message"));
    }

    @Test
    public void d_testCannotDeleteAdminUser() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, adminToken);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.delete(usersUrl + "/" + adminUserId, headers);
        Assert.assertFalse(resp.getBoolean(Constants.SUCCESS));
        Assert.assertEquals("Cannot delete user with role : " + Role.ADMIN.getRole(), resp.getString("message"));
    }

    @Test
    public void e_testCannotDeleteUserManagerUser() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, userManagerToken);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.delete(usersUrl + "/" + userManagerUserId, headers);
        Assert.assertFalse(resp.getBoolean(Constants.SUCCESS));
        Assert.assertEquals("Cannot delete user with role : " + Role.USER_MANAGER.getRole(), resp.getString("message"));
    }

    @Test
    public void f_testGetMealsOfUser1AsUser2() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, user2Token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.get(usersUrl + "/" + userId1 + "/meals", headers);
        Assert.assertFalse(resp.getBoolean(Constants.SUCCESS));
        Assert.assertEquals("User cannot perform the operation", resp.getString("message"));
    }

    @Test
    public void g_testGetMealsOfUser1AsUserManager() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, userManagerToken);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.get(usersUrl + "/" + userId1 + "/meals", headers);
        Assert.assertFalse(resp.getBoolean(Constants.SUCCESS));
        Assert.assertEquals("User cannot perform the operation", resp.getString("message"));
    }

    @Test
    public void h_testGetMealsOfUser1AsAdmin() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, adminToken);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.get(usersUrl + "/" + userId1 + "/meals", headers);
        Assert.assertTrue(resp.getBoolean(Constants.SUCCESS));
        Assert.assertTrue(resp.getJSONArray(Constants.MEALS).length() > 0);
    }

    @Test
    public void i_testCreateMealsForUser1AsUser2() throws Exception {
        Meal meal = new Meal();
        meal.setItemName(name);
        meal.setCalories(calories);
        meal.setEmail(email1);

        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, user2Token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.post(mealsUrl, headers, GsonUtil.toJsonObject(meal));
        Assert.assertFalse(resp.getBoolean(Constants.SUCCESS));
        Assert.assertEquals("User cannot perform the operation", resp.getString("message"));
    }

    @Test
    public void j_testCreateMealsForUser1AsUserManager() throws Exception {
        Meal meal = new Meal();
        meal.setItemName(name);
        meal.setCalories(calories);
        meal.setEmail(email1);

        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, userManagerToken);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.post(mealsUrl, headers, GsonUtil.toJsonObject(meal));
        Assert.assertFalse(resp.getBoolean(Constants.SUCCESS));
        Assert.assertEquals("User cannot perform the operation", resp.getString("message"));
    }

    @Test
    public void k_testCreateMealsForUser1AsAdmin() throws Exception {
        Meal meal = new Meal();
        meal.setItemName(name);
        meal.setCalories(calories);
        meal.setEmail(email1);

        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, adminToken);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.post(mealsUrl, headers, GsonUtil.toJsonObject(meal));
        Assert.assertEquals(name, resp.getString(Constants.ITEM_NAME));
        Assert.assertEquals(calories, resp.getDouble(Constants.CALORIES), 0);
    }

    @Test
    public void l_testUpdateMealsForUser1AsUser2() throws Exception {
        String newName = name + "xz";
        float newcalories = 908;
        Meal meal = new Meal();
        meal.setMealId(mealId1);
        meal.setItemName(newName);
        meal.setCalories(newcalories);
        meal.setEmail(email1);

        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, user2Token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.put(mealsUrl + "/" + mealId1, headers, GsonUtil.toJsonObject(meal));
        Assert.assertFalse(resp.getBoolean(Constants.SUCCESS));
        Assert.assertEquals("User cannot perform the operation", resp.getString("message"));
    }

    @Test
    public void m_testUpdateMealsForUser1AsUserManager() throws Exception {
        String newName = name + "xz";
        float newcalories = 908;
        Meal meal = new Meal();
        meal.setMealId(mealId1);
        meal.setItemName(newName);
        meal.setCalories(newcalories);
        meal.setEmail(email1);

        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, userManagerToken);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.put(mealsUrl + "/" + mealId1, headers, GsonUtil.toJsonObject(meal));
        Assert.assertFalse(resp.getBoolean(Constants.SUCCESS));
        Assert.assertEquals("User cannot perform the operation", resp.getString("message"));
    }

    @Test
    public void n_testUpdateMealsForUser1AsAdmin() throws Exception {
        String newName = name + "xz";
        float newcalories = 908;
        Meal meal = new Meal();
        meal.setMealId(mealId1);
        meal.setItemName(newName);
        meal.setCalories(newcalories);
        meal.setEmail(email1);

        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, adminToken);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.put(mealsUrl + "/" + mealId1, headers, GsonUtil.toJsonObject(meal));
        Assert.assertEquals(email1, resp.getString(Constants.EMAIL));
        Assert.assertEquals(newName, resp.getString(Constants.ITEM_NAME));
        Assert.assertEquals(mealId1, resp.getString("mealId"));
        Assert.assertEquals(newcalories, resp.getDouble(Constants.CALORIES),0);
    }

    @Test
    public void o_testDeleteMealsForUser1AsUser2() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, user2Token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.delete(mealsUrl + "/" + mealId1, headers);
        Assert.assertFalse(resp.getBoolean(Constants.SUCCESS));
        Assert.assertEquals("User cannot perform the operation", resp.getString("message"));
    }

    @Test
    public void p_testDeleteMealsForUser1AsUserManager() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, userManagerToken);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.delete(mealsUrl + "/" + mealId1, headers);
        Assert.assertFalse(resp.getBoolean(Constants.SUCCESS));
        Assert.assertEquals("User cannot perform the operation", resp.getString("message"));
    }

    @Test
    public void q_testDeleteMealsForUser1AsAdmin() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, adminToken);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.delete(mealsUrl + "/" + mealId1, headers);
        Assert.assertEquals(resp.toString(),"{}");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        // delete test users after test with adminToken and userManager token for test coverage
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, adminToken);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.delete(usersUrl + "/" + userId1, headers);
        Assert.assertEquals(resp.toString(),"{}");

        headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, userManagerToken);
        util = new HttpUtil();
        resp = util.delete(usersUrl + "/" + userId2, headers);
        Assert.assertEquals(resp.toString(),"{}");
    }
}
