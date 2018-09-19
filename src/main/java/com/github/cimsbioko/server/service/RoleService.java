package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.Privilege;
import com.github.cimsbioko.server.domain.Role;
import com.github.cimsbioko.server.domain.User;
import com.github.cimsbioko.server.exception.ConstraintViolations;

import java.util.List;

public interface RoleService {

    String NOROLE_VALUE = "norole";

    Role evaluateRole(Role entityItem) throws ConstraintViolations;

    Role convertAndSetPrivileges(Role entityItem, List<String> privileges);

    boolean checkDuplicateRoleName(String name);

    List<Privilege> getPrivileges();

    /**
     * Find a list of all {@link User}'s who currently have the {@link Role} <code>role</code>
     *
     * @param role the {@link Role} to filter {@link User}'s on
     * @return a list of {@link User}'s who currently have the {@link Role} <code>role</code>
     */
    List<User> findUsersWithRole(Role role);

    /**
     * Retrieve a list of {@link Role}'s that are active, excluding <code>selectedRole</code>
     *
     * @param selectedRole the role to exclude from the list of results
     * @return a list of all active {@link Role}'s excluding <code>selectedRole</code>
     */
    List<Role> findRolesExcluding(Role selectedRole);

    /**
     * Remove <code>oldRole</code> from all <code>users</code> and optionally assign
     * a new role to the user if it contains a valid {@link Role} uuid at the same index.
     *
     * @param users   a list of users to remove oldRole from, and optionally assign a new role to
     * @param roles   a list of role uuid's, where <code>users</code> at index i will receive <code>roles</code> at index i. If there
     *                is no role re-assignment, then the <code>roles</code> value at index i should be {@link RoleService#NOROLE_VALUE}
     * @param oldRole the role that should be remove from all <code>users</code>
     */
    void updateUserRoles(User[] users, String[] roles, Role oldRole);
}
