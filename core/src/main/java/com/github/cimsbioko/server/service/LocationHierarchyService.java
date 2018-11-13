package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.LocationHierarchy;
import org.springframework.security.access.prepost.PreAuthorize;

public interface LocationHierarchyService {

    @PreAuthorize("hasAuthority('CREATE_HIERARCHY')")
    LocationHierarchy createMap(String localityUuid, String mapUuid, String mapName);

    @PreAuthorize("hasAuthority('CREATE_HIERARCHY')")
    LocationHierarchy createOrFindMap(String localityUuid, String mapName);

    @PreAuthorize("hasAuthority('CREATE_HIERARCHY')")
    LocationHierarchy createSector(String mapUuid, String sectorUuid, String sectorName);

    @PreAuthorize("hasAuthority('CREATE_HIERARCHY')")
    LocationHierarchy createOrFindSector(String mapUuid, String sectorName);

}
