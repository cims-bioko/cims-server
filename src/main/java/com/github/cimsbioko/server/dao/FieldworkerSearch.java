package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.FieldWorker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FieldworkerSearch {
    Page<FieldWorker> findBySearch(String query, Pageable page);
}
