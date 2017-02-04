package com.github.cimsbioko.server.controller.service;

import java.util.List;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.PrivilegeConstants;
import com.github.cimsbioko.server.domain.model.Round;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;

public interface RoundService {

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    void evaluateRound(Round round) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<Round> getAllRounds();
}