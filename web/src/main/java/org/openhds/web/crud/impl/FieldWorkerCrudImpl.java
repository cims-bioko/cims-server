package org.openhds.web.crud.impl;

import org.openhds.controller.exception.AuthorizationException;
import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.domain.model.FieldWorker;
import org.openhds.controller.service.refactor.FieldWorkerService;
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
        showListing=false;
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
    	}
    	catch(ConstraintViolations e) {
    		jsfService.addError(e.getMessage());
    	} catch(AuthorizationException e) {
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

