package com.github.cimsbioko.server.controller.service;

import java.util.List;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.OutMigration;
import com.github.cimsbioko.server.domain.model.PrivilegeConstants;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Individual;

public interface OutMigrationService {

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    void evaluateOutMigrationBeforeCreate(OutMigration outMigration) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    List<OutMigration> getOutMigrations(Individual individual);

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    void createOutMigration(OutMigration outMigration) throws ConstraintViolations;
}
