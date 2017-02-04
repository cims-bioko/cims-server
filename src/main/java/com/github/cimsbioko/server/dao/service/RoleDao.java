package com.github.cimsbioko.server.dao.service;

import java.util.List;

import com.github.cimsbioko.server.domain.model.Role;
import com.github.cimsbioko.server.domain.model.User;

public interface RoleDao extends Dao<Role, String> {

    List<User> findAllUsersWithRole(Role role);

    List<Role> findAllRolesExcept(Role role);
}
