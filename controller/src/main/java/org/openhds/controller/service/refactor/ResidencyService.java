package org.openhds.controller.service.refactor;


import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Location;
import org.openhds.domain.model.Residency;

import java.util.List;

public interface ResidencyService extends EntityService<Residency> {

    boolean hasOpenResidency(Individual individual);
    List<Residency> getResidenciesByLocation(Location location);

}
