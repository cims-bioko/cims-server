package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Individual;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface IndividualRepository extends PagingAndSortingRepository<Individual, String> {
    List<Individual> findByExtIdAndDeletedIsNull(String extId);
}
