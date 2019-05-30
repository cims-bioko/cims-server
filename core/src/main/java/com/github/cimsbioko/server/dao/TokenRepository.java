package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.AccessToken;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TokenRepository extends PagingAndSortingRepository<AccessToken, String> {
}
