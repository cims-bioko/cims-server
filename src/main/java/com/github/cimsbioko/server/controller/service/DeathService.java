package com.github.cimsbioko.server.controller.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.Death;
import com.github.cimsbioko.server.domain.model.Membership;
import com.github.cimsbioko.server.domain.model.PrivilegeConstants;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.model.SocialGroup;

public interface DeathService {

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    Death evaluateDeath(Death entityItem) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    Death createDeath(Death entityItem) throws ConstraintViolations, SQLException;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<Death> getDeathsByIndividual(Individual individual);

    @Authorized({PrivilegeConstants.DELETE_ENTITY})
    void deleteDeath(Death entityItem);

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    void createDeathAndSetNewHead(Death death, List<SocialGroup> groups, List<Individual> successors, HashMap<Integer, List<Membership>> memberships) throws Exception;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    boolean checkDuplicateIndividual(Individual indiv);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    boolean checkHeadOfSocialGroup(Individual indiv);
}
