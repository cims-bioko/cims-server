package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.FormSubmission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Stream;

public interface FormSubmissionRepository extends PagingAndSortingRepository<FormSubmission, String>, FormSubmissionSearch {

    @Query("select f from #{#entityName} f where f.processed is null order by date_trunc('hour', f.submitted), f.collected")
    Stream<FormSubmission> findUnprocessed(Pageable pageable);

    List<FormSubmission> findByFormIdAndSubmittedAfter(String formId, Timestamp submitted, Pageable pageable);

    List<FormSubmission> findByFormId(String formId, Pageable pageable);

    long deleteByFormIdAndFormVersion(String formId, String formVersion);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update #{#entityName} s set s.processed = null where s.campaignId = :campaign and s.formBinding = :binding")
    int markUnprocessed(@Param("campaign") String campaign, @Param("binding") String binding);
}
