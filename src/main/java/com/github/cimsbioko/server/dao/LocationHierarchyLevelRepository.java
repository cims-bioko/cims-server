package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.LocationHierarchyLevel;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface LocationHierarchyLevelRepository extends PagingAndSortingRepository<LocationHierarchyLevel, String> {
    LocationHierarchyLevel findByKeyId(int key);
}
