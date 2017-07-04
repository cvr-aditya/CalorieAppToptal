package com.toptal.calorie.auth;


import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mysql.jdbc.StringUtils;
import com.toptal.calorie.constant.Constants;
import com.toptal.calorie.dao.UserDAO;
import com.toptal.calorie.model.User;
import com.toptal.calorie.util.JwtUtil;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.hibernate.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;


/**
 * Created by asirna on 24/06/2017.
 */
public class CustomAuthenticator implements Authenticator<AuthenticationCredentials, AuthUser> {

    private UserDAO userDAO;
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticator.class);

    public CustomAuthenticator(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    @UnitOfWork
    public Optional<AuthUser> authenticate(AuthenticationCredentials authenticationCredentials) throws AuthenticationException {
        AuthUser authUser = null;
        User user;
        if (StringUtils.isNullOrEmpty(authenticationCredentials.getToken())) {
            LOGGER.error("Token is null/empty");
            throw new WebApplicationException("Unable to validate credentials", Response.Status.UNAUTHORIZED);
        }
        try {
            DecodedJWT decodedJWT = JwtUtil.decodeJWT(authenticationCredentials.getToken());
            String userId = decodedJWT.getClaim(Constants.USER_ID).asString();
            user = userDAO.getById(userId);
        } catch (JWTVerificationException exception) {
            LOGGER.error("Token verification failed {}", exception);
            throw new WebApplicationException("Unable to validate credentials", Response.Status.UNAUTHORIZED);
        }
        if (user != null) {
            authUser = new AuthUser(user.getName(), user.getRole());
        }
        return Optional.ofNullable(authUser);
    }
}
