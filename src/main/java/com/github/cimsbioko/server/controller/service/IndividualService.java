package com.github.cimsbioko.server.controller.service;

import java.util.List;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.domain.model.PrivilegeConstants;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Individual;

public interface IndividualService {

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    Individual evaluateIndividual(Individual entityItem) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    void validateGeneralIndividual(Individual indiv) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    String generateIdWithBound(Individual entityItem, int count) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.CREATE_ENTITY, PrivilegeConstants.EDIT_ENTITY})
    String getLatestEvent(Individual individual);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    Individual validateIdLength(Individual entityItem) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<String> getIndividualExtIds(String term);

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    Individual generateId(Individual entityItem) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    Individual findIndivById(String indivExtId, String msg) throws Exception;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    Individual findIndivById(String indivExtId);

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    Individual createTemporaryIndividualWithExtId(String extId, FieldWorker CollectedBy) throws Exception;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<Individual> getAllIndividuals();

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    void createIndividual(Individual individual) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    long getTotalIndividualCount();

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<Individual> getAllIndividualsInRange(Individual start, int size);
}

