package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.LocationHierarchyLevel;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface LocationHierarchyLevelRepository extends PagingAndSortingRepository<LocationHierarchyLevel, String> {
    Optional<LocationHierarchyLevel> findByKeyId(int key);
}
