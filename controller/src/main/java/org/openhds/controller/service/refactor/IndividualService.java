package org.openhds.controller.service.refactor;


import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.domain.model.Individual;

public interface IndividualService extends EntityService<Individual> {

    boolean isDeceased(Individual individual);

    int getExistingExtIdCount(String extId);

    Individual getUnknownIndividual() throws ConstraintViolations;

}
