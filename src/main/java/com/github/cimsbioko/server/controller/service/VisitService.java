package com.github.cimsbioko.server.controller.service;

import java.util.List;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.PrivilegeConstants;
import com.github.cimsbioko.server.domain.model.Visit;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;

public interface VisitService {

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    Visit evaluateVisit(Visit entityItem) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    Visit generateId(Visit entityItem) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.EDIT_ENTITY})
    Visit checkVisit(Visit persistedItem, Visit entityItem) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<String> getVisitExtIds(String term);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    Visit findVisitByExtId(String extId);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    Visit findVisitByUuid(String uuid);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    void validateGeneralVisit(Visit visit) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    void createVisit(Visit visit) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<Visit> getAllVisits();

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<Visit> getAllVisitsForRoundInRange(int round, Visit start, int pageSize);

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    long getTotalVisitCountForRound(int roundNumber);
}
