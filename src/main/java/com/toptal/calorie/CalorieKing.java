package com.toptal.calorie;


import com.toptal.calorie.auth.AuthenticationFilter;
import com.toptal.calorie.auth.CustomAuthenticator;
import com.toptal.calorie.dao.MealDAO;
import com.toptal.calorie.dao.UserDAO;
import com.toptal.calorie.model.Meal;
import com.toptal.calorie.model.User;
import com.toptal.calorie.resource.MealResource;
import com.toptal.calorie.resource.TokenResource;
import com.toptal.calorie.resource.UserResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import java.io.File;

/**
 * Created by asirna on 23/06/2017.
 */
public class CalorieKing extends Application<AppConfig> {


    public static void main(String[] args) throws Exception {
        String configPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" +
                File.separator + "resources" + File.separator + "config.yml";
        new CalorieKing().run("server", configPath);
    }

    private final HibernateBundle<AppConfig> hibernate = new HibernateBundle<AppConfig>(User.class, Meal.class) {
        @Override
        public PooledDataSourceFactory getDataSourceFactory(AppConfig appConfig) {
            return appConfig.getDataSourceFactory();
        }
    };

    @Override
    public String getName() {
        return "CalorieKing";
    }

    @Override
    public void initialize(Bootstrap<AppConfig> bootstrap) {
        bootstrap.addBundle(hibernate);
        bootstrap.addBundle(new AssetsBundle("/CalorieKing", "/" , "/index.html"));
    }

    @Override
    public void run(AppConfig appConfig, Environment environment) throws Exception {
        final UserDAO userDAO = new UserDAO(hibernate.getSessionFactory());
        final MealDAO mealDAO = new MealDAO(hibernate.getSessionFactory());

        CustomAuthenticator authenticator = new UnitOfWorkAwareProxyFactory(hibernate)
                .create(CustomAuthenticator.class, new Class<?>[]{ UserDAO.class}, new Object[]{ userDAO });
        AuthenticationFilter filter = new AuthenticationFilter(authenticator);

        environment.jersey().register(new AuthDynamicFeature(filter));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new UserResource(userDAO, mealDAO));
        environment.jersey().register(new TokenResource(userDAO));
        environment.jersey().register(new MealResource(mealDAO, userDAO));
        environment.jersey().setUrlPattern("/api/*");
    }
}
