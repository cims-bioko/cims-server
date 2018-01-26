package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.Location;
import com.github.cimsbioko.server.domain.LocationHierarchy;
import com.github.cimsbioko.server.domain.LocationHierarchyLevel;
import com.github.cimsbioko.server.exception.ConstraintViolations;
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

    @PreAuthorize("hasAuthority('CREATE_ENTITY')")
    LocationHierarchy createOrFindMap(String localityUuid, String mapName);

    @PreAuthorize("hasAuthority('CREATE_ENTITY')")
    LocationHierarchy createMap(String localityUuid, String mapUuid, String mapName);

    @PreAuthorize("hasAuthority('CREATE_ENTITY')")
    LocationHierarchy createOrFindSector(String mapUuid, String sectorName);

    @PreAuthorize("hasAuthority('CREATE_ENTITY')")
    LocationHierarchy createSector(String mapUuid, String sectorUuid, String sectorName);
}

