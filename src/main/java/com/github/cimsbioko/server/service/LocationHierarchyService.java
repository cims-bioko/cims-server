package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.LocationHierarchy;
import org.springframework.security.access.prepost.PreAuthorize;

public interface LocationHierarchyService {

    @PreAuthorize("hasAuthority('CREATE_ENTITY')")
    LocationHierarchy createMap(String localityUuid, String mapUuid, String mapName);

    @PreAuthorize("hasAuthority('CREATE_ENTITY')")
    LocationHierarchy createSector(String mapUuid, String sectorUuid, String sectorName);

}
