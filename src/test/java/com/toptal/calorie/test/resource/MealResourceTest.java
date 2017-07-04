package com.toptal.calorie.test.resource;

import com.toptal.calorie.constant.Constants;
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
 * Create a user and do CRUD meals for that user
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MealResourceTest {

    private static final String baseUrl = "http://localhost:8080/api";
    private static final String loginUrl = baseUrl + "/auth/login";
    private static final String usersUrl = baseUrl + "/users";
    private static final String mealsUrl = baseUrl + "/meals";
    private static String userId = null;
    private static String email = null;
    private static String token = null;
    private static String mealId = null;
    private static String itemName = "itemName";
    private static float calories = 67;

    @BeforeClass
    public static void setUp() throws Exception {
        //Create a user
        String name = "UserResourceTest";
        String password = "password";
        String role = "user";
        email = "UserResourceTest" + System.currentTimeMillis() + "@gmail.com";
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
        Assert.assertEquals(email, resp.getString(Constants.EMAIL));
        Assert.assertEquals(name, resp.getString(Constants.NAME));
        Assert.assertEquals(role, resp.getString(Constants.ROLE));

        // login with the user created
        body = new JSONObject();
        body.put(Constants.EMAIL, email);
        body.put(Constants.PASSWORD, password);
        headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        util = new HttpUtil();
        resp = util.post(loginUrl, headers, body);
        token = resp.getString(Constants.TOKEN);
        Assert.assertEquals(email, resp.getJSONObject(Constants.PROFILE).getString(Constants.EMAIL));
        Assert.assertEquals(name, resp.getJSONObject(Constants.PROFILE).getString(Constants.NAME));
        Assert.assertEquals(role, resp.getJSONObject(Constants.PROFILE).getString(Constants.ROLE));
        Assert.assertEquals(userId, resp.getJSONObject(Constants.PROFILE).getString("userId"));
    }

    @Test
    public void a_testCreateMeal() throws Exception {
        Meal meal = new Meal();
        meal.setItemName(itemName);
        meal.setCalories(calories);
        meal.setEmail(email);

        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.post(mealsUrl, headers, GsonUtil.toJsonObject(meal));
        mealId = resp.getString("mealId");
        Assert.assertEquals(itemName, resp.getString(Constants.ITEM_NAME));
        Assert.assertEquals(calories, resp.getDouble(Constants.CALORIES), 0);
        Assert.assertEquals(email, resp.getString(Constants.EMAIL));
    }

    @Test
    public void b_getMealById() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.get(mealsUrl + "/" + mealId, headers);
        Assert.assertEquals(itemName, resp.getString(Constants.ITEM_NAME));
        Assert.assertEquals(calories, resp.getDouble(Constants.CALORIES), 0);
        Assert.assertEquals(email, resp.getString(Constants.EMAIL));
        Assert.assertEquals(mealId, resp.getString("mealId"));
    }

    @Test
    public void c_updateMeal() throws Exception {
        String newName = itemName + "xz";
        float calories = 90;
        Meal meal = new Meal();
        meal.setMealId(mealId);
        meal.setItemName(newName);
        meal.setCalories(calories);
        meal.setEmail(email);

        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.put(mealsUrl + "/" + mealId, headers, GsonUtil.toJsonObject(meal));
        Assert.assertEquals(email, resp.getString(Constants.EMAIL));
        Assert.assertEquals(newName, resp.getString(Constants.ITEM_NAME));
        Assert.assertEquals(mealId, resp.getString("mealId"));
        Assert.assertEquals(calories, resp.getDouble(Constants.CALORIES),0);
    }

    @Test
    public void d_testGetAllMeals() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.get(mealsUrl, headers);
        Assert.assertEquals(403, resp.getInt("code"));
        Assert.assertEquals("User not authorized.", resp.getString("message"));
    }

    @Test
    public void e_getMealsOfUser() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.get(usersUrl + "/" + userId + "/meals", headers);
        Assert.assertTrue(resp.getBoolean(Constants.SUCCESS));
        Assert.assertTrue(resp.getJSONArray(Constants.MEALS).length() > 0);
    }

    @Test
    public void f_deleteMeal() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.delete(mealsUrl + "/" + mealId, headers);
        Assert.assertEquals(resp.toString(),"{}");
        resp = util.get(mealsUrl + "/" + mealId, headers);
        Assert.assertEquals(false, resp.getBoolean(Constants.SUCCESS));
        Assert.assertEquals("Failed to fetch meal info", resp.getString("message"));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(Constants.X_AUTH_TOKEN, token);
        HttpUtil util = new HttpUtil();
        JSONObject resp = util.delete(usersUrl + "/" + userId, headers);
        Assert.assertEquals(resp.toString(),"{}");
        resp = util.get(usersUrl + "/" + userId, headers);
        Assert.assertEquals(401, resp.getInt("code"));
        Assert.assertEquals("Unable to validate credentials", resp.getString("message"));
    }
}
