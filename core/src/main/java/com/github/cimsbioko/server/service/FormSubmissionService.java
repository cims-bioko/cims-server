package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.FormSubmission;
import com.github.cimsbioko.server.exception.ExistingSubmissionException;

import java.util.stream.Stream;

public interface FormSubmissionService {

    FormSubmission recordSubmission(FormSubmission submission, String deprecatedId) throws ExistingSubmissionException;

    Stream<FormSubmission> getUnprocessed(int batchSize);

    void markProcessed(FormSubmission submission, Boolean processedOk);
}
