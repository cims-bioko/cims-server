package com.github.cimsbioko.server.controller.service;

import java.util.List;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.PrivilegeConstants;
import com.github.cimsbioko.server.domain.model.Residency;
import com.github.cimsbioko.server.domain.model.Individual;

/**
 * A service class to be used when dealing with the Residency class
 * This service provides various methods to verify the integrity of a Residency in the system
 *
 * @author Dave
 */
public interface ResidencyService {

    /**
     * Determine if an Individual has a current open residency. An open residency is defined as a residency
     * that has no end date
     *
     * @param individual the individual to check for an open residency
     * @return true if the individual has atleast 1 open residency, false otherwise
     */
    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    boolean hasOpenResidency(Individual individual);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<Residency> getAllResidencies(Individual individual);

}
