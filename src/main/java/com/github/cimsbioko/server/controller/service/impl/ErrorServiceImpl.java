package com.github.cimsbioko.server.controller.service.impl;

import com.github.cimsbioko.server.controller.service.ErrorService;
import com.github.cimsbioko.server.controller.service.FieldWorkerService;
import com.github.cimsbioko.server.dao.ErrorDao;
import com.github.cimsbioko.server.domain.model.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ErrorServiceImpl implements ErrorService {

    @Autowired
    private ErrorDao errorDao;

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Override
    public Error logError(Error error) {
        if (null == error.getFieldWorker()) {
            error.setFieldWorker(fieldWorkerService.getUnknownFieldWorker());
        }
        errorDao.save(error);
        return error;
    }

}
