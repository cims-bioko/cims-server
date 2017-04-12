package com.github.cimsbioko.server.controller.service;

import com.github.cimsbioko.server.domain.model.FormSubmission;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FormSubmissionService {

    @Transactional(readOnly = true)
    List<FormSubmission> getUnprocessed(int batchSize);

    @Transactional
    void markProcessed(FormSubmission submission);
}
