package org.openhds.errorhandling.endpoint.impl;

import org.openhds.errorhandling.dao.ErrorLogDAO;
import org.openhds.domain.model.ErrorLog;
import org.openhds.errorhandling.endpoint.ErrorServiceEndPoint;
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
