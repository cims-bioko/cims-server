package com.github.cimsbioko.server.errorhandling;

import java.util.List;

import com.github.cimsbioko.server.controller.service.FieldWorkerService;
import com.github.cimsbioko.server.domain.model.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ErrorServiceImpl implements ErrorService {

    @Autowired
    private List<ErrorEndPoint> errorEndPoints;

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Override
    public Error logError(Error error) {
        if (null == error.getFieldWorker()) {
            error.setFieldWorker(fieldWorkerService.getUnknownFieldWorker());
        }

        for (ErrorEndPoint errorEndPoint : errorEndPoints) {
            errorEndPoint.logError(error);
        }

        return error;
    }

}
