package org.openhds.controller.service.refactor;


import org.openhds.domain.model.Individual;

public interface IndividualService extends EntityService<Individual> {

    boolean isDeceased(Individual individual);

    int getExistingExtIdCount(String extId);

    String generateChildExtId(Individual mother);

}
