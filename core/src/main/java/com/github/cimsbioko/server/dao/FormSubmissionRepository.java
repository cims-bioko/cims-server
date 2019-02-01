package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.FormSubmission;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.sql.Timestamp;
import java.util.List;

public interface FormSubmissionRepository extends PagingAndSortingRepository<FormSubmission, String> {
    List<FormSubmission> findByProcessedNullOrderByCollected(Pageable pageable);
    List<FormSubmission> findByFormIdAndSubmittedAfter(String formId, Timestamp submitted, Pageable pageable);
    List<FormSubmission> findByFormId(String formId, Pageable pageable);
}
