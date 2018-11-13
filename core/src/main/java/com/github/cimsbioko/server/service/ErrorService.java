package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.FormSubmission;

public interface ErrorService {
    void logError(FormSubmission submission, String message);
}
