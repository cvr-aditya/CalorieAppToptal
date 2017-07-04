package com.toptal.calorie.model;

import com.google.gson.annotations.Expose;
import com.toptal.calorie.constant.Constants;
import com.toptal.calorie.constant.SqlQuery;
import com.toptal.calorie.util.GsonUtil;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * Created by asirna on 27/06/2017.
 */

@Entity
@Table(name = "meals")
@NamedQueries(
        {
                @NamedQuery(
                        name = Constants.GET_MEALS_OF_USER,
                        query = SqlQuery.GET_MEALS_OF_USER
                ),
                @NamedQuery(
                        name = Constants.GET_ALL_MEALS,
                        query = SqlQuery.GET_ALL_MEALS
                ),
                @NamedQuery(
                        name = Constants.DELETE_MEAL,
                        query = SqlQuery.DELETE_MEAL
                ),
                @NamedQuery(
                        name = Constants.GET_MEALS_DATE_RANGE,
                        query = SqlQuery.GET_MEALS_DATE_RANGE
                ),
                @NamedQuery(
                        name = Constants.GET_MEAL_TIME_RANGE,
                        query = SqlQuery.GET_MEAL_TIME_RANGE
                )
        }
)
public class Meal {

    @Expose
    @Id
    @Column(name = "meal_id", nullable = false)
    private String mealId;

    @Expose
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Expose
    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Expose
    @Column(name = "calories", nullable = false)
    private float calories;

    @Column(name = "meal_time", nullable = false)
    private String mealTime;

    @Transient
    @Expose
    private String date;

    @Transient
    @Expose
    private String time;

    @Expose
    @Column(name = "email")
    private String email;

    public String getMealId() {
        return mealId;
    }

    public void setMealId(String mealId) {
        this.mealId = mealId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public float getCalories() {
        return calories;
    }

    public void setCalories(float calories) {
        this.calories = calories;
    }

    public String getMealTime() {
        return mealTime;
    }

    public void setMealTime(String mealTime) {
        this.mealTime = mealTime;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Meal)) return false;
        Meal meals = (Meal) o;
        return Float.compare(meals.getCalories(), getCalories()) == 0 &&
                Objects.equals(getMealId(), meals.getMealId()) &&
                Objects.equals(getUserId(), meals.getUserId()) &&
                Objects.equals(getItemName(), meals.getItemName()) &&
                Objects.equals(getMealTime(), meals.getMealTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMealId(), getUserId(), getItemName(), getCalories(), getMealTime());
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
