package com.github.cimsbioko.server.controller.service.refactor;


import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;

public interface FieldWorkerService extends EntityService<FieldWorker> {


    void generatePasswordHash(FieldWorker fieldWorker) throws ConstraintViolations;

    void generateIdPrefix(FieldWorker fieldWorker);

    FieldWorker generateId(FieldWorker fieldWorker) throws ConstraintViolations;

}
