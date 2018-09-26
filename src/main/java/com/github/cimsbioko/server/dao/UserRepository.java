package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.User;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<User, String> {
    User findByUsername(String username);
}
