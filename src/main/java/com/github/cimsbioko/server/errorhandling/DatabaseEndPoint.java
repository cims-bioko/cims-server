package com.github.cimsbioko.server.errorhandling;

import com.github.cimsbioko.server.domain.model.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseEndPoint implements ErrorEndPoint {

    @Autowired
    private ErrorDao errorDao;

    @Override
    public void logError(Error error) {
        errorDao.save(error);
    }

}
