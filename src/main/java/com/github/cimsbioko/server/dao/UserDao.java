package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.User;

import java.util.List;

public interface UserDao extends Dao<User, String> {

    List<User> findByUsername(String username);
}
