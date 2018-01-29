package com.github.cimsbioko.server.dao.impl;

import com.github.cimsbioko.server.dao.ErrorDao;
import com.github.cimsbioko.server.domain.Error;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("errorDao")
public class ErrorDaoImpl implements ErrorDao {

    private static final Logger logger = LoggerFactory.getLogger(ErrorDaoImpl.class);

    @Autowired
    private SessionFactory sf;

    public void save(Error error) {
        sf.getCurrentSession().persist(error);
    }
}
