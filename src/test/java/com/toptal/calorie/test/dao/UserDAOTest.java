package com.toptal.calorie.test.dao;

import com.toptal.calorie.dao.UserDAO;
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
public class UserDAOTest {

    @ClassRule
    public static DAOTestRule daoTestRule = DAOTestRule.newBuilder()
            .addEntityClass(User.class)
            .build();

    private static UserDAO userDAO;
    private static User user;
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
    }

    @Test
    public void a_createUser() {
        final User dbUser = daoTestRule.inTransaction(() -> userDAO.create(user));
        assertThat(dbUser.getUserId().equals(userId));
        assertThat(dbUser.getEmail().equals(name));
        assertThat(dbUser.getPassword().equals(password));
        Assert.assertEquals(dbUser.getTargetCalories(),calories, 0);
    }

    @Test
    public void b_getUserById() {
        final User dbUser = daoTestRule.inTransaction(() -> userDAO.getById(userId));
        assertThat(dbUser.getUserId().equals(userId));
        assertThat(dbUser.getEmail().equals(name));
        assertThat(dbUser.getPassword().equals(password));
        Assert.assertEquals(dbUser.getTargetCalories(),calories, 0);
    }

    @Test
    public void c_getUserByEmail() {
        final List<User> dbUser = daoTestRule.inTransaction(() -> userDAO.getByEmail(email));
        Assert.assertEquals(dbUser.size(),1);
        assertThat(dbUser.get(0).getUserId().equals(userId));
        assertThat(dbUser.get(0).getEmail().equals(email));
        assertThat(dbUser.get(0).getPassword().equals(password));
        Assert.assertEquals(dbUser.get(0).getTargetCalories(),calories, 0);
    }

    @Test
    public void d_updateUser() {
        float newcal = 100;
        String newname = "newname";
        user.setTargetCalories(newcal);
        user.setName(newname);

        daoTestRule.inTransaction(() -> userDAO.update(user));
        User dbUser = daoTestRule.inTransaction(() -> userDAO.getById(userId));
        assertThat(dbUser.getUserId().equals(userId));
        assertThat(dbUser.getEmail().equals(newname));
        assertThat(dbUser.getPassword().equals(password));
        Assert.assertEquals(dbUser.getTargetCalories(),newcal, 0);

        //revert back old values
        user.setTargetCalories(calories);
        user.setName(name);

        daoTestRule.inTransaction(() -> userDAO.update(user));
        dbUser = daoTestRule.inTransaction(() -> userDAO.getById(userId));
        assertThat(dbUser.getUserId().equals(userId));
        assertThat(dbUser.getEmail().equals(name));
        assertThat(dbUser.getPassword().equals(password));
        Assert.assertEquals(dbUser.getTargetCalories(),calories, 0);
    }

    @Test
    public void e_updateUserToken() {

        User dbUser = daoTestRule.inTransaction(() -> userDAO.getById(userId));
        Assert.assertNull(dbUser.getToken());
        String token = "token";
        int isUpdated = daoTestRule.inTransaction(() -> userDAO.updateUserToken(userId, token));
        Assert.assertEquals(isUpdated, 1);
    }

    @Test
    public void f_deleteUser() {
        final int isDeleted = daoTestRule.inTransaction(() -> userDAO.delete(userId));
        Assert.assertEquals(isDeleted, 1);
    }

    @Test(expected = IdentifierGenerationException.class)
    public void g_createWithNullUserName() {
        User u = new User();
        u.setUserId(null);
        daoTestRule.inTransaction(() -> userDAO.create(u));
    }

    @Test(expected = ConstraintViolationException.class)
    public void h_createWithDuplicateEmail() {
        User u = new User();
        u.setUserId(email);
        daoTestRule.inTransaction(() -> userDAO.create(u));
    }
}
