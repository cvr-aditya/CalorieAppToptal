package com.toptal.calorie.model;

import com.google.gson.annotations.Expose;
import com.toptal.calorie.util.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

/**
 * Created by asirna on 23/06/2017.
 */
public class ErrorResponse {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorResponse.class);

    @Expose
    private boolean success = false;

    @Expose
    private String message;

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }

    public static Response getInvalidInputResponse(User user) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Invalid input");
        LOGGER.error("Invalid input {}", user);
        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
    }

    public static Response getInvalidInputResponse(Meal meal) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Invalid input");
        LOGGER.error("Invalid input {}", meal);
        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse.toString()).build();
    }

    public static Response getInvalidPermResponse() {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("User cannot perform the operation");
        return Response.status(Response.Status.UNAUTHORIZED).entity(errorResponse.toString()).build();
    }
}
