package org.openhds.controller.service;

import java.util.List;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.domain.annotations.Authorized;
import org.openhds.domain.model.FieldWorker;
import org.openhds.domain.model.PrivilegeConstants;

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
