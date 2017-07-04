package com.toptal.calorie.auth;


import com.toptal.calorie.constant.Constants;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by asirna on 23/06/2017.
 */

@PreMatching
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter extends AuthFilter<AuthenticationCredentials, AuthUser> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);
    private CustomAuthenticator authenticator;

    public AuthenticationFilter(CustomAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void filter(ContainerRequestContext containerRequestContext) throws IOException {

        Optional<AuthUser> authUser;
        MultivaluedMap<String,String> headers = containerRequestContext.getHeaders();
        if (!(headers.containsKey(Constants.X_AUTH_TOKEN))) {
            LOGGER.error("X-Auth-Token missing in headers");
            throw new WebApplicationException("Unable to validate credentials", Response.Status.UNAUTHORIZED);
        }
        try {
            String authToken = headers.getFirst(Constants.X_AUTH_TOKEN);
            LOGGER.debug("Auth token is :: " + authToken);
            AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials();
            authenticationCredentials.setToken(authToken);
            authUser = authenticator.authenticate(authenticationCredentials);
        } catch (AuthenticationException exception) {
            LOGGER.error("authentication exception {}", exception);
            throw new WebApplicationException("Unable to validate credentials", Response.Status.UNAUTHORIZED);
        }
        if (authUser.isPresent()) {
            SecurityContext securityContext = new AuthSecurityContext(authUser.get(), containerRequestContext.getSecurityContext());
            containerRequestContext.setSecurityContext(securityContext);
        } else {
            throw new WebApplicationException("Unable to validate credentials", Response.Status.UNAUTHORIZED);
        }
    }
}
