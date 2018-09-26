package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Role;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RoleRepository extends PagingAndSortingRepository<Role, String> {
}
