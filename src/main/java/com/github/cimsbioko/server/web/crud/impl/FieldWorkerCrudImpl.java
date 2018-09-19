package com.github.cimsbioko.server.web.crud.impl;

import com.github.cimsbioko.server.domain.FieldWorker;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.refactor.FieldWorkerService;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.util.StringUtils.isEmpty;

public class FieldWorkerCrudImpl extends EntityCrudImpl<FieldWorker, String> {

    @Autowired
    private FieldWorkerService fieldWorkerService;

    public FieldWorkerCrudImpl(Class<FieldWorker> entityClass) {
        super(entityClass);
    }

    @Override
    public String create() {

        try {
            fieldWorkerService.generateId(entityItem);
            fieldWorkerService.generatePasswordHash(entityItem);
            fieldWorkerService.generateIdPrefix(entityItem);
            fieldWorkerService.isEligibleForCreation(entityItem, new ConstraintViolations());
            return super.create();
        } catch (ConstraintViolations e) {
            jsfService.addError("exception.validation", e.getLocalizedMessage());
        }
        return null;
    }

    public String edit() {
        try {
            if (!isEmpty(entityItem.getPassword())) {
                fieldWorkerService.generatePasswordHash(entityItem);
            }
            return super.edit();
        } catch (ConstraintViolations e) {
            jsfService.addError("exception.validation", e.getLocalizedMessage());
        }
        return null;
    }

    public FieldWorkerService getFieldWorkerService() {
        return fieldWorkerService;
    }

    public void setFieldWorkerService(FieldWorkerService fieldWorkerService) {
        this.fieldWorkerService = fieldWorkerService;
    }
}

