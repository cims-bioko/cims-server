package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CampaignSearch {
    Page<Campaign> findBySearch(String query, Pageable page);
}
