package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.model.*;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import org.springframework.security.access.prepost.PreAuthorize;

public interface LocationHierarchyService {

    @PreAuthorize("hasAuthority('VIEW_ENTITY')")
    LocationHierarchy findByUuid(String uuid);

    @PreAuthorize("hasAuthority('VIEW_ENTITY')")
    LocationHierarchy findByExtId(String extId);

    @PreAuthorize("hasAuthority('CREATE_ENTITY')")
    void createLocationHierarchy(LocationHierarchy locationHierarchy) throws ConstraintViolations;

    @PreAuthorize("hasAuthority('VIEW_ENTITY')")
    LocationHierarchyLevel getLevel(int level);

    @PreAuthorize("hasAuthority('CREATE_ENTITY')")
    Location generateId(Location entityItem) throws ConstraintViolations;
}

