package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.model.FormSubmission;

import java.util.List;

public interface FormSubmissionDao {

    void save(FormSubmission submission);
    void delete(String uuid);
    FormSubmission findById(String uuid);
    List<FormSubmission> findByForm(String formId, String formVersion);
}
