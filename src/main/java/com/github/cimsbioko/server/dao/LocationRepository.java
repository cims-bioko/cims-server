package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Location;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface LocationRepository extends PagingAndSortingRepository<Location, String> {
    List<Location> findByExtIdAndDeletedIsNull(String extId);
}
