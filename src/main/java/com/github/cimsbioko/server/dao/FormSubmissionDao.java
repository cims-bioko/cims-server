package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.FormSubmission;

import java.sql.Timestamp;
import java.util.List;

public interface FormSubmissionDao {
    void save(FormSubmission submission);
    void delete(String uuid);
    FormSubmission findById(String uuid);
    List<FormSubmission> findUnprocessed(int batchSize);
    List<FormSubmission> findRecent(String form, String version, String binding, String device, Integer limit);
    List<FormSubmission> find(String form, String version, String binding, String device, Timestamp submittedSince, Integer limit, boolean sortDesc);
    void markProcessed(FormSubmission submission, Boolean processedOk);
}
