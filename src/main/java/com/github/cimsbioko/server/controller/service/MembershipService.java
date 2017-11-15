package com.github.cimsbioko.server.controller.service;

import java.util.List;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.model.Membership;
import com.github.cimsbioko.server.domain.model.PrivilegeConstants;
import com.github.cimsbioko.server.domain.model.SocialGroup;

public interface MembershipService {

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    Membership evaluateMembershipBeforeCreate(Membership entityItem) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    Membership updateMembership(Membership entityItem) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    boolean checkDuplicateMembership(Individual indiv, SocialGroup group);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<Membership> getAllMemberships(Individual indiv);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<Membership> getAllMemberships();

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    Membership createMembershipForPregnancyOutcome(Individual individual, SocialGroup sg, FieldWorker fw, String relationToGroupHead);

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    void validateGeneralMembership(Membership membership) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    void createMembership(Membership item) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<Membership> getAllMembershipsInRange(Membership start, int size);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    long getTotalMembershipCount();


}
