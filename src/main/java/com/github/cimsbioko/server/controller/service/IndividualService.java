package com.github.cimsbioko.server.controller.service;

import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Individual;
import org.springframework.security.access.prepost.PreAuthorize;

public interface IndividualService {

    @PreAuthorize("hasAuthority('CREATE_ENTITY')")
    Individual evaluateIndividual(Individual entityItem) throws ConstraintViolations;

    @PreAuthorize("hasAuthority('VIEW_ENTITY')")
    Individual validateIdLength(Individual entityItem) throws ConstraintViolations;

    @PreAuthorize("hasAuthority('CREATE_ENTITY')")
    Individual generateId(Individual entityItem) throws ConstraintViolations;

    @PreAuthorize("hasAuthority('VIEW_ENTITY')")
    Individual findIndivById(String indivExtId);

}

