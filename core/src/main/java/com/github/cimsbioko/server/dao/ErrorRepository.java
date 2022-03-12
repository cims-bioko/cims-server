package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Error;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ErrorRepository extends PagingAndSortingRepository<Error, String> {
    Optional<Error> findFirstBySubmissionInstanceIdOrderByCreatedDesc(@Param("instance") String instance);
}
