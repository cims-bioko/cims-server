package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.FormSubmission;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Stream;

public interface FormSubmissionRepository extends PagingAndSortingRepository<FormSubmission, String> {
    @Query(value = "select f from #{#entityName} f where f.processed is null order by f.collected asc")
    Stream<FormSubmission> findUnprocessed(Pageable pageable);
    List<FormSubmission> findByFormIdAndSubmittedAfter(String formId, Timestamp submitted, Pageable pageable);
    List<FormSubmission> findByFormId(String formId, Pageable pageable);
    long deleteByFormIdAndFormVersion(String formId, String formVersion);
}
