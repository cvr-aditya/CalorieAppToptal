package com.toptal.calorie.resource;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.toptal.calorie.constant.Constants;
import com.toptal.calorie.dao.UserDAO;
import com.toptal.calorie.model.ErrorResponse;
import com.toptal.calorie.model.User;
import com.toptal.calorie.util.GsonUtil;
import com.toptal.calorie.util.JwtUtil;
import io.dropwizard.hibernate.UnitOfWork;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by asirna on 25/06/2017.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class TokenResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenResource.class);
    private final UserDAO userDAO;

    public TokenResource(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @POST
    @UnitOfWork
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(String body) {
        try {
            JSONObject loginBody = new JSONObject(body);
            String email = loginBody.getString(Constants.EMAIL);
            List<User> users = userDAO.getByEmail(email);
            String password = "";
            if (loginBody.has(Constants.PASSWORD)) {
                password = loginBody.getString(Constants.PASSWORD);
            }
            if (users.size() == 1) {
                User user = users.get(0);
                if (loginBody.has(Constants.PROVIDER)) {
                    if (user.getProvider().equals(Constants.GOOGLE) && loginBody.getString(Constants.PROVIDER).
                            equals(Constants.GOOGLE)) {
                        String authToken = JwtUtil.createTokenForUser(user);
                        user.setToken(authToken);
                        JSONObject response = new JSONObject();
                        response.put(Constants.PROFILE, new JSONObject(GsonUtil.toJson(user)));
                        response.put(Constants.SUCCESS, true);
                        response.put(Constants.TOKEN, authToken);
                        return Response.status(Response.Status.OK).entity(response.toString()).build();
                    }
                    else {
                        LOGGER.error("Invalid provider");
                        ErrorResponse errorResponse = new ErrorResponse();
                        errorResponse.setMessage("Login failed. Invalid authentication provider");
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                                entity(errorResponse.toString()).build();
                    }
                }
                if (password.equals(user.getPassword())) {
                    String authToken = JwtUtil.createTokenForUser(user);
                    int isTokenUpdated = userDAO.updateUserToken(user.getUserId(), authToken);
                    if (isTokenUpdated == 1) {
                        user.setToken(authToken);
                        JSONObject response = new JSONObject();
                        response.put(Constants.PROFILE, new JSONObject(GsonUtil.toJson(user)));
                        response.put(Constants.SUCCESS, true);
                        response.put(Constants.TOKEN, authToken);
                        return Response.status(Response.Status.OK).entity(response.toString()).build();
                    } else { // Token not updated in db
                        LOGGER.error("Failed to update token in db");
                        ErrorResponse errorResponse = new ErrorResponse();
                        errorResponse.setMessage("Login failed");
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                                entity(errorResponse.toString()).build();
                    }
                }
            }
            //Password mismatch or invalid email
            LOGGER.error("Failed to login because of invalid email/password");
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Invalid email/password");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
        } catch (JSONException exception) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Invalid input");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
        }
    }

    @POST
    @UnitOfWork
    @Path("/logout")
    public Response logout(@HeaderParam(Constants.X_AUTH_TOKEN) String authToken) {
        try {
            DecodedJWT decodedJWT = JwtUtil.decodeJWT(authToken);
            String userId = decodedJWT.getClaim(Constants.USER_ID).asString();
            int isTokenUpdated = userDAO.updateUserToken(userId, null);
            if (isTokenUpdated == 1) {
                return Response.ok().build();
            } else { // Token not updated in db
                LOGGER.error("Failed to update token in db");
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setMessage("Logout failed");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                        entity(errorResponse.toString()).build();
            }
        }
        catch (JWTVerificationException exception) {
            LOGGER.error("Failed to verify token {}", exception);
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setMessage("Logout failed");
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
        }
    }
}
