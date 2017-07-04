package com.toptal.calorie.model;

import com.google.gson.annotations.Expose;
import com.toptal.calorie.constant.Constants;
import com.toptal.calorie.constant.SqlQuery;
import com.toptal.calorie.util.GsonUtil;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by asirna on 23/06/2017.
 */


@Entity
@Table(name = "users")
@NamedQueries(
        {
                @NamedQuery(
                        name = Constants.FIND_ALL_USERS,
                        query = SqlQuery.GET_ALL_USERS
                ),
                @NamedQuery(
                        name = Constants.FIND_USER_BY_EMAIL,
                        query = SqlQuery.GET_BY_EMAIL
                ),
                @NamedQuery(
                        name = Constants.UPDATE_USER_TOKEN,
                        query = SqlQuery.UPDATE_TOKEN
                ),
                @NamedQuery(
                        name = Constants.DELETE_USER,
                        query = SqlQuery.DELETE_USER
                )
        }
)
public class User {

    @Expose
    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Expose
    @Column(name = "name", nullable = false)
    private String name;

    @Expose
    @Column(name = "email", nullable = false)
    private String email;

    @Expose
    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "token")
    private String token;

    @Expose
    @Column(name = "provider")
    private String provider;

    @Expose
    @Column(name = "target_calories")
    private float targetCalories;

    @Column(name = "password", nullable = false)
    private String password;

    @Expose
    @Column(name = "created_at")
    private String timestamp;

    @Expose
    @Column(name = "updated_at")
    private String updatedTimestamp;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public float getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(float targetCalories) {
        this.targetCalories = targetCalories;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUpdatedTimestamp(String updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public String getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Float.compare(user.getTargetCalories(), getTargetCalories()) == 0 &&
                Objects.equals(getUserId(), user.getUserId()) &&
                Objects.equals(getName(), user.getName()) &&
                Objects.equals(getEmail(), user.getEmail()) &&
                Objects.equals(getRole(), user.getRole()) &&
                Objects.equals(getToken(), user.getToken()) &&
                Objects.equals(getPassword(), user.getPassword()) &&
                Objects.equals(getTimestamp(), user.getTimestamp()) &&
                Objects.equals(getUpdatedTimestamp(), user.getUpdatedTimestamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getName(), getEmail(), getRole(), getToken(),
                getTargetCalories(), getPassword(), getTimestamp(), getUpdatedTimestamp());
    }

    @Override
    public String toString() {
        return GsonUtil.toJson(this);
    }
}
