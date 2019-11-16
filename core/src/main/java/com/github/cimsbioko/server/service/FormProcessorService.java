package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.FormSubmission;

public interface FormProcessorService {
    void process(FormSubmission submission);
}
