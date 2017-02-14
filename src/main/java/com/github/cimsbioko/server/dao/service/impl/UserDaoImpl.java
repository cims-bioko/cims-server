package com.github.cimsbioko.server.dao.service.impl;

import com.github.cimsbioko.server.dao.service.UserDao;
import com.github.cimsbioko.server.domain.model.User;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class UserDaoImpl extends BaseDaoImpl<User, String> implements UserDao {

    public UserDaoImpl(Class<User> entityType) {
        super(entityType);
    }

    @Override
    public List<User> findByUsername(String username) {
        return addImplicitRestrictions(getSession().createCriteria(User.class))
                .add(Restrictions.eq("username", username)).list();
    }
}
