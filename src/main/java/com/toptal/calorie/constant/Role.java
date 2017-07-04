package com.toptal.calorie.constant;

/**
 * Created by asirna on 24/06/2017.
 */
public enum Role {

    USER("user"),
    USER_MANAGER("user_manager"),
    ADMIN("admin");

    private String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}