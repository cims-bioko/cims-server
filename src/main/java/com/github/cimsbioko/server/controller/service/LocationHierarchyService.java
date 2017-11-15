package com.github.cimsbioko.server.controller.service;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.LocationHierarchy;
import com.github.cimsbioko.server.domain.model.LocationHierarchyLevel;
import com.github.cimsbioko.server.domain.model.PrivilegeConstants;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Location;

public interface LocationHierarchyService {

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    LocationHierarchy findByUuid(String uuid);

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    LocationHierarchy findByExtId(String extId);

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    void createLocationHierarchy(LocationHierarchy locationHierarchy) throws ConstraintViolations;

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    LocationHierarchyLevel getLevel(int level);

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    Location generateId(Location entityItem) throws ConstraintViolations;
}

