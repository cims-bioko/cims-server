package com.github.cimsbioko.server.errorhandling.endpoint.impl;

import com.github.cimsbioko.server.errorhandling.dao.ErrorDao;
import com.github.cimsbioko.server.domain.model.Error;
import com.github.cimsbioko.server.errorhandling.endpoint.ErrorServiceEndPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseEndPoint implements ErrorServiceEndPoint {

    @Autowired
    private ErrorDao errorDao;

    @Override
    public void logError(Error error) {
        errorDao.createError(error);
    }

}
