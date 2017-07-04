package com.toptal.calorie.auth;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

/**
 * Created by asirna on 24/06/2017.
 */
public class AuthSecurityContext implements SecurityContext {

    private final AuthUser authUser;
    private final SecurityContext securityContext;

    public AuthSecurityContext(AuthUser authUser, SecurityContext securityContext) {
        this.authUser = authUser;
        this.securityContext = securityContext;
    }

    public Principal getUserPrincipal() {
        return authUser;
    }

    public boolean isUserInRole(String role) {
        return role.equals(authUser.getRole());
    }

    public boolean isSecure() {
        return securityContext.isSecure();
    }

    public String getAuthenticationScheme() {
        return "TOPTAL_TOKEN";
    }
}
