package com.github.cimsbioko.server.service.refactor.crudhelpers;

import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.refactor.FieldWorkerService;
import com.github.cimsbioko.server.domain.model.FieldWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("FieldWorkerCrudHelper")
public class FieldWorkerCrudHelper extends AbstractEntityCrudHelperImpl<FieldWorker> {

    @Autowired
    FieldWorkerService fieldWorkerService;

    @Override
    protected void preCreateSanityChecks(FieldWorker fieldWorker) throws ConstraintViolations {
    }

    @Override
    protected void cascadeReferences(FieldWorker fieldWorker) throws ConstraintViolations {
        fieldWorkerService.generateId(fieldWorker);
        fieldWorkerService.generatePasswordHash(fieldWorker);
        fieldWorkerService.generateIdPrefix(fieldWorker);
    }

    @Override
    protected void validateReferences(FieldWorker fieldWorker) throws ConstraintViolations {
        ConstraintViolations constraintViolations = new ConstraintViolations();
        if (!fieldWorkerService.isEligibleForCreation(fieldWorker, constraintViolations)) {
            throw (constraintViolations);
        }
    }

    @Override
    public List<FieldWorker> getAll() {
        return genericDao.findAll(FieldWorker.class, true);
    }

    @Override
    public FieldWorker getByExtId(String id) {
        return genericDao.findByProperty(FieldWorker.class, "extId", id, true);
    }

    @Override
    public FieldWorker getByUuid(String id) {
        return genericDao.findByProperty(FieldWorker.class, "uuid", id);
    }
}
