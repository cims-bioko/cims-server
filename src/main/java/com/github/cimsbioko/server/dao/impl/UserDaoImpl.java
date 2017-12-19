package com.github.cimsbioko.server.dao.impl;

import com.github.cimsbioko.server.dao.UserDao;
import com.github.cimsbioko.server.domain.User;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("userDao")
public class UserDaoImpl extends BaseDaoImpl<User, String> implements UserDao {

    public UserDaoImpl() {
        super(User.class);
    }

    @Override
    public List<User> findByUsername(String username) {
        return addImplicitRestrictions(getSession().createCriteria(User.class))
                .add(Restrictions.eq("username", username)).list();
    }
}
