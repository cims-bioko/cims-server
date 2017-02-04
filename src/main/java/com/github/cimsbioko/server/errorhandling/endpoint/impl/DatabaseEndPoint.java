package com.github.cimsbioko.server.errorhandling.endpoint.impl;

import com.github.cimsbioko.server.errorhandling.dao.ErrorLogDAO;
import com.github.cimsbioko.server.domain.model.ErrorLog;
import com.github.cimsbioko.server.errorhandling.endpoint.ErrorServiceEndPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseEndPoint implements ErrorServiceEndPoint {

    @Autowired
    private ErrorLogDAO errorLogDAO;

    @Override
    public void logError(ErrorLog errorLog) {
        errorLogDAO.createErrorLog(errorLog);
    }

}
