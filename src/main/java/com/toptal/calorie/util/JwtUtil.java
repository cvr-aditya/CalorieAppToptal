package com.toptal.calorie.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.toptal.calorie.constant.Constants;
import com.toptal.calorie.constant.Role;
import com.toptal.calorie.model.User;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by asirna on 25/06/2017.
 */
public class JwtUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);

    private static final String HMAC_SECRET = "secret";
    private static final String TOKEN_ISSUER = "toptal";
    private static final int TOKEN_EXPIRY_MINUTES = 60;
    private static Algorithm algorithm;

    static {
        try {
            algorithm = Algorithm.HMAC512(HMAC_SECRET);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Failed to create jwt token {}", e);
        }
    }

    public static String createTokenForUser(User user) {
        Date currentTime = new Date();
        return JWT
                .create()
                .withClaim(Constants.USER_ID, user.getUserId())
                .withClaim(Constants.ROLE, user.getRole())
                .withClaim(Constants.EMAIL, user.getEmail())
                .withIssuedAt(currentTime)
                .withExpiresAt(DateUtils.addMinutes(currentTime, TOKEN_EXPIRY_MINUTES))
                .withIssuer(TOKEN_ISSUER)
                .sign(algorithm);
    }

    public static DecodedJWT decodeJWT(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(algorithm).withIssuer(TOKEN_ISSUER).build();
        return verifier.verify(token);
    }

    public static String getTimestamp() {
        Date date = new Date();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        String dateString = dt.format(date);
        return dateString;
    }

    public static String getDate(String date) {
        SimpleDateFormat mysqlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        Date dt = null;
        try {
            dt = mysqlFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = newFormat.format(dt);
        return dateString;
    }

    public static String getTime(String date) {
        SimpleDateFormat mysqlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        Date dt = null;
        try {
            dt = mysqlFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat newFormat = new SimpleDateFormat("HH:mm");
        String dateString = newFormat.format(dt);
        return dateString;
    }

    public static String getNextDate(String  curDate) {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(curDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return format.format(calendar.getTime());
    }

    public static void checkFilterDateFormat(String filterDate) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.parse(filterDate);
    }

    public static Date checkFilterTimeFormat(String filterTime) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.parse(filterTime);
    }

    public static String getUserIdFromToken(String token) throws JWTVerificationException {
        return decodeJWT(token).getClaim(Constants.USER_ID).asString();
    }

    public static String getRoleFromToken(String token) throws JWTVerificationException {
        return decodeJWT(token).getClaim(Constants.ROLE).asString();
    }

    public static boolean isAdminToken(String token) throws JWTVerificationException {
        return getRoleFromToken(token).equals(Role.ADMIN.getRole());
    }

    public static boolean isUserToken(String token) throws JWTVerificationException {
        return getRoleFromToken(token).equals(Role.USER.getRole());
    }
}
