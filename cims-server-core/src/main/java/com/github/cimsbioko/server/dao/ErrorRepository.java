package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Error;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ErrorRepository extends PagingAndSortingRepository<Error, String> {
}
