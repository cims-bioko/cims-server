package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.exception.ExistingSubmissionException;
import com.github.cimsbioko.server.domain.model.FormSubmission;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FormSubmissionService {

    @Transactional
    FormSubmission recordSubmission(FormSubmission submission) throws ExistingSubmissionException;

    @Transactional(readOnly = true)
    List<FormSubmission> getUnprocessed(int batchSize);

    @Transactional
    void markProcessed(FormSubmission submission, Boolean processedOk);
}
