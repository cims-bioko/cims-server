package com.github.cimsbioko.server.web.crud.impl;

import com.github.cimsbioko.server.controller.exception.AuthorizationException;
import com.github.cimsbioko.server.controller.service.refactor.FieldWorkerService;
import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import org.springframework.beans.factory.annotation.Autowired;

public class FieldWorkerCrudImpl extends EntityCrudImpl<FieldWorker, String> {

    @Autowired
    private FieldWorkerService fieldWorkerService;

    public FieldWorkerCrudImpl(Class<FieldWorker> entityClass) {
        super(entityClass);
    }

    @Override
    public String createSetup() {
        reset(false, true);
        showListing = false;
        entityItem = newInstance();
        navMenuBean.setNextItem(entityClass.getSimpleName());
        navMenuBean.addCrumb(entityClass.getSimpleName() + " Create");
        return outcomePrefix + "_create";
    }


    @Override
    public String create() {

        try {
            fieldWorkerService.generateId(entityItem);
            fieldWorkerService.generatePasswordHash(entityItem);
            fieldWorkerService.generateIdPrefix(entityItem);
            fieldWorkerService.isEligibleForCreation(entityItem, new ConstraintViolations());
            return super.create();
        } catch (ConstraintViolations | AuthorizationException e) {
            jsfService.addError(e.getMessage());
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

