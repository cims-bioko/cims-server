package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Set;

public interface RoleRepository extends PagingAndSortingRepository<Role, String> {
    Set<Role> findByUuidIn(Set<String> uuids);
    Page<Role> findByDeletedIsNull(Pageable pageable);
    Set<Role> findByDeletedIsNull();
}
