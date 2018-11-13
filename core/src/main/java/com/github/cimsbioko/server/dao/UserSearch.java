package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserSearch {
    Page<User> findBySearch(String query, Pageable page);
}
