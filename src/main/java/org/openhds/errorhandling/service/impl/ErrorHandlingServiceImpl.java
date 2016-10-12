package org.openhds.errorhandling.service.impl;

import java.util.List;

import org.openhds.controller.service.FieldWorkerService;
import org.openhds.domain.model.ErrorLog;
import org.openhds.errorhandling.endpoint.ErrorServiceEndPoint;
import org.openhds.errorhandling.service.ErrorHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ErrorHandlingServiceImpl implements ErrorHandlingService {

    @Autowired
    private List<ErrorServiceEndPoint> errorEndPoints;

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Override
    public ErrorLog logError(ErrorLog error) {
        if (null == error.getFieldWorker()) {
            error.setFieldWorker(fieldWorkerService.getUnknownFieldWorker());
        }

        for (ErrorServiceEndPoint errorEndPoint : errorEndPoints) {
            errorEndPoint.logError(error);
        }

        return error;
    }

}
