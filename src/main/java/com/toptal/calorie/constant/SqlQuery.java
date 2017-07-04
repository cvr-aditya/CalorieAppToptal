package com.toptal.calorie.constant;

/**
 * Created by asirna on 23/06/2017.
 */
public class SqlQuery {

    public static final String GET_ALL_USERS = "Select user from User user order by timestamp desc";
    public static final String GET_BY_EMAIL = "Select user from User user where user.email = :" + Constants.EMAIL;
    public static final String UPDATE_TOKEN = "Update User set token = :" + Constants.TOKEN + " where userId = :"
            + Constants.USER_ID;
    public static final String DELETE_USER  = "delete from User where userId = :" + Constants.USER_ID;

    public static final String GET_MEALS_OF_USER = "Select meal from Meal meal where meal.userId = :"
            + Constants.USER_ID;
    public static final String GET_ALL_MEALS = "Select meal from Meal meal";
    public static final String DELETE_MEAL = "delete from Meal where mealId = :" + Constants.MEAL_ID;
    public static final String GET_MEALS_DATE_RANGE = "Select meal from Meal meal where userId = :" + Constants.USER_ID
            + " and mealTime >= :" + Constants.FROM_DATE + " and mealTime <= :" + Constants.TO_DATE;
    public static final String GET_MEAL_TIME_RANGE = "Select meal from Meal meal where userId = :" + Constants.USER_ID
            + " and TIME(mealTime) >= :" + Constants.FROM_TIME + " and TIME(mealTime) <= :" + Constants.TO_TIME;

}
