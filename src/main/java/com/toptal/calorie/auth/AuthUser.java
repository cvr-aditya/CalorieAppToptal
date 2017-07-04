package com.toptal.calorie.auth;

import com.toptal.calorie.constant.Role;

import java.security.Principal;

/**
 * Created by asirna on 24/06/2017.
 */
public class AuthUser implements Principal {

    private final String name;
    private final String role;

    public AuthUser(String name, String role) {
        this.name = name;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}
