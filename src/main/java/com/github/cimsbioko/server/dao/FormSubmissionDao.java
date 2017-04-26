package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.model.FormSubmission;

import java.util.List;

public interface FormSubmissionDao {
    void save(FormSubmission submission);
    void delete(String uuid);
    FormSubmission findById(String uuid);
    List<FormSubmission> findByForm(String formId, String formVersion);
    List<FormSubmission> findUnprocessed(int batchSize);
    List<FormSubmission> findRecent(String form, String version, String binding, String device, Integer limit);
    void markProcessed(FormSubmission submission, Boolean processedOk);
}
