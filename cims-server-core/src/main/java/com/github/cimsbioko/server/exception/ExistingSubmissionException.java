package com.github.cimsbioko.server.exception;

import com.github.cimsbioko.server.domain.FormSubmission;

public class ExistingSubmissionException extends Exception {

    private final FormSubmission submission;

    public ExistingSubmissionException(String message, FormSubmission submission) {
        super(message);
        this.submission = submission;
    }

    public FormSubmission getSubmission() {
        return submission;
    }
}
