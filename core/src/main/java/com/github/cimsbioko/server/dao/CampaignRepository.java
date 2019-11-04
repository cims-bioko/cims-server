package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Campaign;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends PagingAndSortingRepository<Campaign, String> {
    @Query("select c from #{#entityName} c where" +
            " (c.end is null or current_date() < c.end) and (c.start is null or current_date() > c.start)" +
            " and c.disabled is null and c.deleted is null")
    List<Campaign> findActive();
    @Query("select c from #{#entityName} c where" +
            " (c.end is null or current_date() < c.end) and (c.start is null or current_date() > c.start)" +
            " and c.disabled is null and c.deleted is null and c.name = :name")
    Optional<Campaign> findActiveByName(@Param("name") String name);
    Optional<Campaign> findByName(String name);
}