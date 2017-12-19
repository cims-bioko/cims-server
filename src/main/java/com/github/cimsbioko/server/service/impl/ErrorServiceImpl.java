package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.service.ErrorService;
import com.github.cimsbioko.server.dao.ErrorDao;
import com.github.cimsbioko.server.domain.model.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ErrorServiceImpl implements ErrorService {

    @Autowired
    private ErrorDao errorDao;

    @Override
    public Error logError(Error error) {
        errorDao.save(error);
        return error;
    }

}
