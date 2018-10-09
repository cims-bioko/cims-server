package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Privilege;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Set;

public interface PrivilegeRepository extends PagingAndSortingRepository<Privilege, String> {
    Set<Privilege> findByUuidIn(Set<String> uuids);
}
