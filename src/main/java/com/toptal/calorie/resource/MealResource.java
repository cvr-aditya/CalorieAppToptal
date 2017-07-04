package com.toptal.calorie.resource;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.mysql.jdbc.StringUtils;
import com.toptal.calorie.constant.Constants;
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
import java.util.List;
import java.util.UUID;

/**
 * Created by asirna on 27/06/2017.
 */

@Path("/meals")
@Produces(MediaType.APPLICATION_JSON)
public class MealResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MealResource.class);
    private final MealDAO mealDAO;
    private final UserDAO userDAO;

    public MealResource(MealDAO mealDAO, UserDAO userDAO) {
        this.mealDAO = mealDAO;
        this.userDAO = userDAO;
    }

    private Response jsonResponse(List<Meal> meals) {
        try {
            JSONObject response = new JSONObject();
            response.put(Constants.MEALS, new JSONArray(GsonUtil.toJson(meals)));
            response.put(Constants.SUCCESS, true);
            return Response.ok(response.toString()).build();
        } catch (JSONException exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to fetch meals");
            return Response.
                    status(Response.Status.BAD_REQUEST).
                    entity(errorResponse.toString()).
                    build();
        }
    }

    @GET
    @UnitOfWork
    @RolesAllowed({"admin"})
    public Response getMeals(@HeaderParam(Constants.X_AUTH_TOKEN) String authToken) {
        try {
            JwtUtil.decodeJWT(authToken);
            return jsonResponse(mealDAO.getAllMeals());
        } catch (JWTVerificationException exception) {
            LOGGER.error("Failed to validate token {}", exception);
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to fetch users");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
        }
    }

    @GET
    @UnitOfWork
    @Path("/{mealid}")
    @PermitAll
    public Response getMeal(@HeaderParam(Constants.X_AUTH_TOKEN) String authToken,
                            @PathParam("mealid") String mealId) {
        try {
            JwtUtil.decodeJWT(authToken);
            Meal meal = mealDAO.getById(mealId);
            if (meal == null) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage("Failed to fetch meal info");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse.toString()).build();
            }
            if (!(JwtUtil.isAdminToken(authToken))) {
                if (!(meal.getUserId().equals(JwtUtil.getUserIdFromToken(authToken)))) {
                    LOGGER.error("userId of meal and userId from token mismatch {} {}", meal.getUserId(),
                            JwtUtil.getUserIdFromToken(authToken));
                    return ErrorResponse.getInvalidPermResponse();
                }
            }
            return Response.status(Response.Status.OK).entity(GsonUtil.toJson(meal)).build();
        } catch (JWTVerificationException exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to fetch meal info");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
        }
    }

    @POST
    @UnitOfWork
    @PermitAll
    public Response createMeal(@HeaderParam(Constants.X_AUTH_TOKEN) String authToken, Meal meal) {
        try {
            JwtUtil.decodeJWT(authToken);
            if (meal.getEmail() == null) {
                return ErrorResponse.getInvalidInputResponse(meal);
            }
            List<User> usersList = userDAO.getByEmail(meal.getEmail());
            if (usersList.size() != 1) { // Invalid input
                LOGGER.error("Invalid email id {}", meal.getEmail());
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage("Invalid e-mail id.Please enter a valid user e-mail");
                return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
            }
            User user = usersList.get(0);
            if (!(JwtUtil.isAdminToken(authToken))) {
                if (!(user.getUserId().equals(JwtUtil.getUserIdFromToken(authToken)))) {
                    LOGGER.error("userId of meal and userId from token mismatch {} {}", user.getUserId(),
                            JwtUtil.getUserIdFromToken(authToken));
                    return ErrorResponse.getInvalidPermResponse();
                }
            }
            meal.setUserId(user.getUserId());
            meal.setMealId(UUID.randomUUID().toString());
            if (StringUtils.isNullOrEmpty(meal.getMealTime())) {
                LOGGER.info("Timestamp from request is null, so setting it");
                meal.setMealTime(JwtUtil.getTimestamp());
            }
            LOGGER.info("Creating meal {}", meal);
            return Response.ok(GsonUtil.toJson(mealDAO.create(meal))).status(Response.Status.CREATED).build();
        } catch (JWTVerificationException exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to create meal");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
        }
    }

    @PUT
    @UnitOfWork
    @Path("/{mealid}")
    @PermitAll
    public Response updateMeal(@HeaderParam(Constants.X_AUTH_TOKEN) String authToken, @PathParam("mealid") String mealId,
                               Meal meal) {
        try {
            JwtUtil.decodeJWT(authToken);
            Meal retrievedMeal = mealDAO.getById(mealId);
            if (meal == null || retrievedMeal == null) {
                return ErrorResponse.getInvalidInputResponse(meal);
            }
            if (!(JwtUtil.isAdminToken(authToken))) {
                if (!(retrievedMeal.getUserId().equals(JwtUtil.getUserIdFromToken(authToken)))) {
                    LOGGER.error("userId of meal and userId from token mismatch {} {}", retrievedMeal.getUserId(),
                            JwtUtil.getUserIdFromToken(authToken));
                    return ErrorResponse.getInvalidPermResponse();
                }
            }
            retrievedMeal.setMealId(mealId);
            if (meal.getCalories() != 0)
                retrievedMeal.setCalories(meal.getCalories());
            if (meal.getItemName() != null)
                retrievedMeal.setItemName(meal.getItemName());
            if (!(StringUtils.isNullOrEmpty(meal.getMealTime()))) {
                LOGGER.info("Time stamp xxx is :: " + meal.getMealTime());
                retrievedMeal.setMealTime(meal.getMealTime());
            }
            LOGGER.info("Updating user {}", retrievedMeal);
            mealDAO.update(retrievedMeal);
            return Response.status(Response.Status.OK).entity(GsonUtil.toJson(retrievedMeal)).build();
        } catch (JWTVerificationException exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to update meal");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
        }

    }

    @DELETE
    @UnitOfWork
    @Path("/{mealid}")
    @PermitAll
    public Response deleteMeal(@HeaderParam(Constants.X_AUTH_TOKEN) String authToken,
                               @PathParam("mealid") String mealId) {
        try {
            JwtUtil.decodeJWT(authToken);
            Meal retrievedMeal = mealDAO.getById(mealId);
            if (!(JwtUtil.isAdminToken(authToken))) {
                if (!(retrievedMeal.getUserId().equals(JwtUtil.getUserIdFromToken(authToken)))) {
                    LOGGER.error("userId of meal and userId from token mismatch {} {}", retrievedMeal.getUserId(),
                            JwtUtil.getUserIdFromToken(authToken));
                    return ErrorResponse.getInvalidPermResponse();
                }
            }
            int isDeleted = mealDAO.delete(mealId);
            if (isDeleted == 1) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            else {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage("Failed to delete meal");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse.toString()).build();
            }
        } catch (JWTVerificationException exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Failed to delete meal");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
        }

    }
}
