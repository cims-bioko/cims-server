package com.github.cimsbioko.server.controller.service;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.*;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;

public interface LocationHierarchyService {

    @Authorized({Privilege.CREATE_ENTITY})
    LocationHierarchy findByUuid(String uuid);

    @Authorized({Privilege.CREATE_ENTITY})
    LocationHierarchy findByExtId(String extId);

    @Authorized({Privilege.CREATE_ENTITY})
    void createLocationHierarchy(LocationHierarchy locationHierarchy) throws ConstraintViolations;

    @Authorized({Privilege.VIEW_ENTITY})
    LocationHierarchyLevel getLevel(int level);

    @Authorized({Privilege.CREATE_ENTITY})
    Location generateId(Location entityItem) throws ConstraintViolations;
}

