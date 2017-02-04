package com.github.cimsbioko.server.dao.service;

import java.util.List;

import com.github.cimsbioko.server.dao.finder.DynamicFinder;
import com.github.cimsbioko.server.domain.model.User;

public interface UserDao extends Dao<User, Long> {

    /**
     * This is mapped to the hibernate mapping file for specifying a
     * query to find a <code>User</code> type by the specified
     * <code>username</code>
     */
    @DynamicFinder("User.findByUsername")
    List<User> findByUsername(String username);
}
