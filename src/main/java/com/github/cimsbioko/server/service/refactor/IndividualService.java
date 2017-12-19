package com.github.cimsbioko.server.service.refactor;

import com.github.cimsbioko.server.domain.model.Individual;

public interface IndividualService extends EntityService<Individual> {

    int getExistingExtIdCount(String extId);

}