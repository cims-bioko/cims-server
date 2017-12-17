package com.github.cimsbioko.server.controller.service;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.Privilege;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Individual;

public interface IndividualService {

    @Authorized({Privilege.CREATE_ENTITY})
    Individual evaluateIndividual(Individual entityItem) throws ConstraintViolations;

    @Authorized({Privilege.VIEW_ENTITY})
    Individual validateIdLength(Individual entityItem) throws ConstraintViolations;

    @Authorized({Privilege.CREATE_ENTITY})
    Individual generateId(Individual entityItem) throws ConstraintViolations;

    @Authorized({Privilege.VIEW_ENTITY})
    Individual findIndivById(String indivExtId);

}

