package com.github.cimsbioko.server.errorhandling.service.impl;

import java.util.List;

import com.github.cimsbioko.server.controller.service.FieldWorkerService;
import com.github.cimsbioko.server.errorhandling.service.ErrorHandlingService;
import com.github.cimsbioko.server.domain.model.ErrorLog;
import com.github.cimsbioko.server.errorhandling.endpoint.ErrorServiceEndPoint;
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
