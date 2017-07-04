package com.toptal.calorie.resource;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.mysql.jdbc.StringUtils;
import com.toptal.calorie.constant.Constants;
import com.toptal.calorie.constant.Role;
import com.toptal.calorie.dao.MealDAO;
import com.toptal.calorie.dao.UserDAO;
import com.toptal.calorie.model.ErrorResponse;
import com.toptal.calorie.model.Meal;
import com.toptal.calorie.model.User;
import com.toptal.calorie.util.GsonUtil;
import com.toptal.calorie.util.JwtUtil;
import io.dropwizard.hibernate.UnitOfWork;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Created by asirna on 23/06/2017.
 */

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);
    private final UserDAO userDAO;
    private final MealDAO mealDAO;

    public UserResource(UserDAO userDAO, MealDAO mealDAO) {
        this.userDAO = userDAO;
        this.mealDAO = mealDAO;
    }

    private Response jsonResponse(List<User> userList) {
        try {
            JSONObject response = new JSONObject();
            response.put(Constants.USERS, new JSONArray(GsonUtil.toJson(userList)));
            response.put(Constants.SUCCESS, true);
            return Response.ok(response.toString()).build();
        } catch (JSONException exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to fetch users");
            return Response.
                    status(Response.Status.BAD_REQUEST).
                    entity(errorResponse.toString()).
                    build();
        }
    }

    @GET
    @UnitOfWork
    @RolesAllowed({"admin", "user_manager"})
    public Response getUsers(@HeaderParam(Constants.X_AUTH_TOKEN) String authToken) {
        try {
            JwtUtil.decodeJWT(authToken);
            return jsonResponse(userDAO.getAll());
        } catch (JWTVerificationException exception) {
            LOGGER.error("Failed to validate token {}", exception);
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to fetch users");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
        }
    }

    @GET
    @UnitOfWork
    @Path("/{userid}")
    @PermitAll
    public Response getUser(@HeaderParam(Constants.X_AUTH_TOKEN) String authToken,
                               @PathParam("userid") String userId) {
        try {
            JwtUtil.decodeJWT(authToken);
            if (JwtUtil.isUserToken(authToken)) {
                if (!(userId.equals(JwtUtil.getUserIdFromToken(authToken)))) {
                    LOGGER.error("userId given and userId from token mismatch {} {}", userId,
                            JwtUtil.getUserIdFromToken(authToken));
                    return ErrorResponse.getInvalidPermResponse();
                }
            }
            User user = userDAO.getById(userId);
            if (user != null) {
                return Response.status(Response.Status.OK).entity(GsonUtil.toJson(user)).build();
            }
            else {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage("Failed to fetch user info");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse.toString()).build();
            }
        } catch (JWTVerificationException exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to fetch user info");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
        }
    }

    @POST
    @UnitOfWork
    public Response createUser(User user) {
        if (user == null || user.getRole() == null) { // Invalid input
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Invalid input");
            LOGGER.error("Invalid input {}", user);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
        if (!(user.getRole().toString().equals(Role.USER.getRole()))) { // Cannot create admin/ user_manager role
            LOGGER.error("Invalid user role {}", user.getRole());
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Invalid user role");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
        List<User> retrievedUserList = userDAO.getByEmail(user.getEmail());
        if (retrievedUserList.size() > 0) { // Email already exists
            User retrievedUser = retrievedUserList.get(0);
            if (retrievedUser.getProvider() != null && retrievedUser.getProvider().equals(Constants.GOOGLE)
                    && user.getProvider() != null && user.getProvider().equals(Constants.GOOGLE)) {
                return Response.ok(GsonUtil.toJson(retrievedUser)).status(Response.Status.CREATED).build();
            } else {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage(String.format("user with email '%s' already exists", user.getEmail()));
                return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
            }
        }
        if (user.getTargetCalories() == 0) {
            user.setTargetCalories(100);
        }
        user.setUserId(UUID.randomUUID().toString());
        String timestamp = JwtUtil.getTimestamp();
        user.setTimestamp(timestamp);
        user.setUpdatedTimestamp(timestamp);
        LOGGER.info("Creating user {}", user);
        return Response.ok(GsonUtil.toJson(userDAO.create(user))).status(Response.Status.CREATED).build();
    }

    @PUT
    @UnitOfWork
    @Path("/{userid}")
    @PermitAll
    public Response updateUser(@HeaderParam(Constants.X_AUTH_TOKEN) String authToken, @PathParam("userid") String userId,
                               User user) {
        try {
            JwtUtil.decodeJWT(authToken);
            User retrievedUser = userDAO.getById(userId);
            if (user == null || retrievedUser == null) {
                return ErrorResponse.getInvalidInputResponse(user);
            }
            if (JwtUtil.isUserToken(authToken)) {
                if (!(userId.equals(JwtUtil.getUserIdFromToken(authToken)))) {
                    LOGGER.error("userId given and userId from token mismatch {} {}", userId,
                            JwtUtil.getUserIdFromToken(authToken));
                    return ErrorResponse.getInvalidPermResponse();
                }
            }
            retrievedUser.setUserId(userId);
            retrievedUser.setUpdatedTimestamp(JwtUtil.getTimestamp());
            if (user.getPassword() != null)
                retrievedUser.setPassword(user.getPassword());
            if (user.getTargetCalories() != 0)
                retrievedUser.setTargetCalories(user.getTargetCalories());
            retrievedUser.setToken(authToken);
            if (user.getName() != null)
                retrievedUser.setName(user.getName());
            LOGGER.info("Updating user {}", retrievedUser);
            userDAO.update(retrievedUser);
            return Response.status(Response.Status.OK).entity(GsonUtil.toJson(retrievedUser)).build();
        } catch (JWTVerificationException exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to update user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
        }

    }

    @DELETE
    @UnitOfWork
    @Path("/{userid}")
    @PermitAll
    public Response deleteUser(@HeaderParam(Constants.X_AUTH_TOKEN) String authToken,
                               @PathParam("userid") String userId) {
        try {
            JwtUtil.decodeJWT(authToken);
            if (JwtUtil.isUserToken(authToken)) {
                if (!(userId.equals(JwtUtil.getUserIdFromToken(authToken)))) {
                    LOGGER.error("userId given and userId from token mismatch {} {}", userId,
                            JwtUtil.getUserIdFromToken(authToken));
                    return ErrorResponse.getInvalidPermResponse();
                }
            }
            User user = userDAO.getById(userId);
            if (user != null && (user.getRole().equals(Role.ADMIN.getRole()) || user.getRole().equals(Role.USER_MANAGER.getRole()))) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage("Cannot delete user with role : " + user.getRole());
                return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
            }
            int isDeleted = userDAO.delete(userId);
            if (isDeleted == 1) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            else {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage("Failed to delete user");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse.toString()).build();
            }
        } catch (JWTVerificationException exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to delete user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
        }
    }

    @GET
    @UnitOfWork
    @Path("/{userid}/meals")
    @PermitAll
    public Response getMeals(@HeaderParam(Constants.X_AUTH_TOKEN) String authToken,
                             @PathParam("userid") String userId,
                             @QueryParam("from") String from,
                             @QueryParam("to") String to,
                             @QueryParam("filterType") String filterType) {
        try {
            JwtUtil.decodeJWT(authToken);
            if (!(JwtUtil.isAdminToken(authToken))) {
                if (!(userId.equals(JwtUtil.getUserIdFromToken(authToken)))) {
                    LOGGER.error("userId of meal and userId from token mismatch {} {}", userId,
                            JwtUtil.getUserIdFromToken(authToken));
                    return ErrorResponse.getInvalidPermResponse();
                }
            }
            List<Meal> meals = new ArrayList<>();
            if (StringUtils.isNullOrEmpty(filterType)) {
                meals = mealDAO.getMealsOfUser(userId);
            }
            else if (filterType.equals("date")) {
                try {
                    JwtUtil.checkFilterDateFormat(from);
                    JwtUtil.checkFilterDateFormat(to);
                    to = JwtUtil.getNextDate(to);
                    meals = mealDAO.getMealsOfUserByDate(userId, from.concat(" 00:00:00"), to.concat(" 00:00:00"));
                } catch (Exception exception) {
                    exception.printStackTrace();
                    ErrorResponse errorResponse = new ErrorResponse();
                    errorResponse.setMessage("Invalid date format.Date should be in yyyy-MM-dd format");
                    return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
                }
            }
            else if (filterType.equals("time")) {
                LOGGER.info("Filtering by time");
                try {
                    Date fromTime = JwtUtil.checkFilterTimeFormat(from);
                    Date toTime = JwtUtil.checkFilterTimeFormat(to);
                    meals = mealDAO.getMealsOfUserByTime(userId, fromTime, toTime);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    ErrorResponse errorResponse = new ErrorResponse();
                    errorResponse.setMessage("Invalid time format. Time should be in HH:mm format");
                    return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
                }
            } else {
                meals = mealDAO.getMealsOfUser(userId);
            }
            try {
                JSONObject response = new JSONObject();
                response.put(Constants.MEALS, new JSONArray(GsonUtil.toJson(meals)));
                response.put(Constants.SUCCESS, true);
                return Response.ok(response.toString()).build();
            } catch (JSONException exception) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage("Failed to fetch meals of user");
                return Response.
                        status(Response.Status.BAD_REQUEST).
                        entity(errorResponse.toString()).
                        build();
            }
        } catch (JWTVerificationException exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to fetch meals of user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
        }
    }

}
