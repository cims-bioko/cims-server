package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.FieldWorker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface FieldWorkerRepository extends PagingAndSortingRepository<FieldWorker, String> {

    Page<FieldWorker> findByDeletedIsNull(Pageable pageable);

    @Query("select case when count(fw) > 0 then true else false end from #{#entityName} fw where fw.extId = :id")
    boolean idExists(@Param("id") String id);
}
