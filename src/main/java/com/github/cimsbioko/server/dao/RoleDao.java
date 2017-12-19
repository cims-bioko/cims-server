package com.github.cimsbioko.server.dao;

import java.util.List;

import com.github.cimsbioko.server.domain.Role;
import com.github.cimsbioko.server.domain.User;

public interface RoleDao extends Dao<Role, String> {

    List<User> findAllUsersWithRole(Role role);

    List<Role> findAllRolesExcept(Role role);
}
