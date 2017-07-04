package com.toptal.calorie.test.dao;

import com.toptal.calorie.dao.MealDAO;
import com.toptal.calorie.dao.UserDAO;
import com.toptal.calorie.model.Meal;
import com.toptal.calorie.model.User;
import com.toptal.calorie.util.JwtUtil;
import io.dropwizard.testing.junit.DAOTestRule;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.id.IdentifierGenerationException;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by asirna on 27/06/2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MealDAOTest {

    @ClassRule
    public static DAOTestRule daoTestRule = DAOTestRule.newBuilder()
            .addEntityClass(Meal.class)
            .addEntityClass(User.class)
            .build();

    private static MealDAO mealDAO;
    private static UserDAO userDAO;
    private static Meal meal;
    private static User user;
    private static String mealId;
    private static String userId;
    private static final String email = "test@gmail.com";
    private static final String name = "name";
    private static final String role = "user";
    private static final String password = "password";
    private static final float calories = 50;

    @BeforeClass
    public static void setUp() throws Exception {
        userDAO = new UserDAO(daoTestRule.getSessionFactory());
        user = new User();
        userId = UUID.randomUUID().toString();
        user.setUserId(userId);
        user.setName(name);
        user.setRole(role);
        user.setEmail(email);
        user.setPassword(password);
        user.setTargetCalories(calories);
        user.setTimestamp(JwtUtil.getTimestamp());
        daoTestRule.inTransaction(() -> userDAO.create(user));

        meal = new Meal();
        mealId = UUID.randomUUID().toString();
        meal.setMealId(mealId);
        meal.setCalories(calories);
        meal.setItemName(name);
        meal.setUserId(userId);
        meal.setMealTime(JwtUtil.getTimestamp());
        mealDAO = new MealDAO(daoTestRule.getSessionFactory());
    }

    @Test
    public void a_createMeal() {
        final Meal dbMeal = daoTestRule.inTransaction(() -> mealDAO.create(meal));
        assertThat(dbMeal.getUserId().equals(userId));
        assertThat(dbMeal.getItemName().equals(name));
        assertThat(dbMeal.getMealId().equals(mealId));
        Assert.assertEquals(dbMeal.getCalories(), calories, 0);
    }

    @Test
    public void b_getMealById() {
        final Meal dbMeal = daoTestRule.inTransaction(() -> mealDAO.getById(mealId));
        assertThat(dbMeal.getUserId().equals(userId));
        assertThat(dbMeal.getItemName().equals(name));
        assertThat(dbMeal.getMealId().equals(mealId));
        Assert.assertEquals(dbMeal.getCalories(), calories, 0);
    }

    @Test
    public void c_updateMeal() {
        float newcal = 100;
        String newname = "newname";

        meal.setItemName(newname);
        meal.setCalories(newcal);
        daoTestRule.inTransaction(() -> mealDAO.update(meal));

        Meal dbMeal = daoTestRule.inTransaction(() -> mealDAO.getById(mealId));
        assertThat(dbMeal.getUserId().equals(userId));
        assertThat(dbMeal.getItemName().equals(newname));
        assertThat(dbMeal.getMealId().equals(mealId));
        Assert.assertEquals(dbMeal.getCalories(), newcal, 0);

        //Revert back
        meal.setItemName(name);
        meal.setCalories(calories);
        daoTestRule.inTransaction(() -> mealDAO.update(meal));

        dbMeal = daoTestRule.inTransaction(() -> mealDAO.getById(mealId));
        assertThat(dbMeal.getUserId().equals(userId));
        assertThat(dbMeal.getItemName().equals(name));
        assertThat(dbMeal.getMealId().equals(mealId));
        Assert.assertEquals(dbMeal.getCalories(), calories, 0);
    }

    @Test
    public void d_getMealsOfUser() {
        List<Meal> dbMeal = daoTestRule.inTransaction(() -> mealDAO.getMealsOfUser(userId));
        Assert.assertEquals(dbMeal.size(), 1);
        assertThat(dbMeal.get(0).getUserId().equals(userId));
        assertThat(dbMeal.get(0).getItemName().equals(name));
        assertThat(dbMeal.get(0).getMealId().equals(mealId));
        Assert.assertEquals(dbMeal.get(0).getCalories(), calories, 0);
    }

    @Test
    public void e_deleteMeal() {
        int isDeleted = daoTestRule.inTransaction(() -> mealDAO.delete(mealId));
        Assert.assertEquals(isDeleted, 1);
    }

    @Test(expected = IdentifierGenerationException.class)
    public void f_createWithNullID() {
        Meal m = new Meal();
        m.setMealId(null);
        daoTestRule.inTransaction(() -> mealDAO.create(m));
    }

}

