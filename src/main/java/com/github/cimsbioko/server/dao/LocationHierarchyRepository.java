package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.LocationHierarchy;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface LocationHierarchyRepository extends PagingAndSortingRepository<LocationHierarchy, String> {
}
