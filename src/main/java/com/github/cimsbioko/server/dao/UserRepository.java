package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<User, String> {

    User findByUsernameAndDeletedIsNull(String username);

    Page<User> findByDeletedIsNull(Pageable pageable);
}
