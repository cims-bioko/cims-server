package com.github.cimsbioko.server.controller.service.refactor;


import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.model.Location;
import com.github.cimsbioko.server.domain.model.Residency;

import java.util.List;

public interface ResidencyService extends EntityService<Residency> {

    boolean hasOpenResidency(Individual individual);

    List<Residency> getResidenciesByLocation(Location location);

}
