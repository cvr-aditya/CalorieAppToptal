package com.toptal.calorie.dao;

import com.toptal.calorie.constant.Constants;
import com.toptal.calorie.model.User;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import java.util.List;

/**
 * Created by asirna on 23/06/2017.
 */
public class UserDAO extends AbstractDAO<User> {

    private SessionFactory sessionFactory;

    public UserDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
    }

    public User getById(String id) {
        return get(id);
    }

    public List<User> getByEmail(String email) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.FIND_USER_BY_EMAIL);
        query.setParameter(Constants.EMAIL, email);
        return query.list();
    }

    public int updateUserToken(String userId, String token) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.UPDATE_USER_TOKEN);
        query.setParameter(Constants.USER_ID, userId);
        query.setParameter(Constants.TOKEN, token);
        return query.executeUpdate();
    }

    public User create(User user) {
        return persist(user);
    }

    public void update(User user) {
        sessionFactory.getCurrentSession().saveOrUpdate(user);
    }

    public int delete(String userId) {
        Query query = sessionFactory.getCurrentSession().getNamedQuery(Constants.DELETE_USER);
        query.setParameter(Constants.USER_ID, userId);
        return query.executeUpdate();
    }

    public List<User> getAll() {
        return list(namedQuery(Constants.FIND_ALL_USERS));
    }

}
