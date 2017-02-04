package com.github.cimsbioko.server.controller.service.refactor;


import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Individual;

public interface IndividualService extends EntityService<Individual> {

    boolean isDeceased(Individual individual);

    int getExistingExtIdCount(String extId);

    Individual getUnknownIndividual() throws ConstraintViolations;

    String generateChildExtId(Individual mother);

}
