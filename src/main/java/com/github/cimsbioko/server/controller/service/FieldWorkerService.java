package com.github.cimsbioko.server.controller.service;

import java.util.List;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.domain.model.PrivilegeConstants;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;

public interface FieldWorkerService {

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    FieldWorker getUnknownFieldWorker();

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    FieldWorker evaluateFieldWorker(FieldWorker entityItem) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<String> getFieldWorkerExtIds(String term);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    FieldWorker findFieldWorkerByExtId(String fieldWorkerId);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<FieldWorker> getAllFieldWorkers();
}
