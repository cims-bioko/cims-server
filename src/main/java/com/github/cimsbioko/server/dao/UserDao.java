package com.github.cimsbioko.server.dao;

import java.util.List;

import com.github.cimsbioko.server.domain.User;

public interface UserDao extends Dao<User, String> {

    List<User> findByUsername(String username);
}
