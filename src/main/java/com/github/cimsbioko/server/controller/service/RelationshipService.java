package com.github.cimsbioko.server.controller.service;

import java.util.List;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.model.PrivilegeConstants;
import com.github.cimsbioko.server.domain.model.Relationship;
import com.github.cimsbioko.server.domain.model.SocialGroup;

public interface RelationshipService {

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    Relationship evaluateRelationship(Relationship entityItem) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    Relationship checkRelationship(Relationship persistedItem, Relationship entityItem) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    boolean checkValidRelationship(Relationship entityItem);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<Relationship> getAllRelationships(Individual individual);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<Relationship> getAllRelationshipsWithinSocialGroup(Individual individual, SocialGroup group);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    void validateGeneralRelationship(Relationship relationship) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<Relationship> getAllRelationships();

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    void createRelationship(Relationship relationship) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<Relationship> getAllRelationshipInRange(Relationship start, int pageSize);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    long getTotalRelationshipCount();
}
