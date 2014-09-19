package org.openhds.controller.service.refactor;


import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Residency;

public interface ResidencyService extends EntityService<Residency> {

    public boolean hasOpenResidency(Individual individual);

}
