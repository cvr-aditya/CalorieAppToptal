package com.toptal.calorie.dao;

import com.toptal.calorie.constant.Constants;
import com.toptal.calorie.model.Meal;
import com.toptal.calorie.util.GsonUtil;
import com.toptal.calorie.util.JwtUtil;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by asirna on 27/06/2017.
 */
public class MealDAO extends AbstractDAO<Meal> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MealDAO.class);
    private SessionFactory sessionFactory;

    public MealDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
    }

    public Meal getById(String mealId) {
        Meal meal = get(mealId);
        if (meal != null)
            return setDateTime(get(mealId));
        return meal;
    }

    public Meal setDateTime(Meal meal) {
        meal.setDate(JwtUtil.getDate(meal.getMealTime()));
        meal.setTime(JwtUtil.getTime(meal.getMealTime()));
        return meal;
    }

    public List<Meal> setDateTime(List<Meal> mealList) {
        for (int i = 0; i < mealList.size(); i++) {
            setDateTime(mealList.get(i));
        }
        return mealList;
    }

    public List<Meal> getAllMeals() {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.GET_ALL_MEALS);
        return setDateTime(query.list());
    }

    public Meal create(Meal meal) {
        return setDateTime(persist(meal));
    }

    public void update(Meal meal) {
        sessionFactory.getCurrentSession().saveOrUpdate(meal);
    }

    public int delete(String mealId) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.DELETE_MEAL);
        query.setParameter(Constants.MEAL_ID, mealId);
        return query.executeUpdate();
    }

    public List<Meal> getMealsOfUser(String userId) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.GET_MEALS_OF_USER);
        query.setParameter(Constants.USER_ID, userId);
        return setDateTime(query.list());
    }

    public List<Meal> getMealsOfUserByDate(String userId, String fromDate, String toDate) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.GET_MEALS_DATE_RANGE);
        query.setParameter(Constants.USER_ID, userId);
        query.setParameter(Constants.FROM_DATE, fromDate);
        query.setParameter(Constants.TO_DATE, toDate);
        LOGGER.info("Running query with userId : " + userId + " fromDate : " + fromDate + " toDate : " + toDate);
        return setDateTime(query.list());
    }

    public  List<Meal> getMealsOfUserByTime(String userId, Date fromTime, Date toTime) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.GET_MEAL_TIME_RANGE);
        query.setParameter(Constants.USER_ID, userId);
        query.setParameter(Constants.FROM_TIME, fromTime);
        query.setParameter(Constants.TO_TIME, toTime);
        LOGGER.info("Running query with userId : " + userId + " fromTime : " + fromTime + " toTime : " + toTime);
        return setDateTime(query.list());
    }
}

