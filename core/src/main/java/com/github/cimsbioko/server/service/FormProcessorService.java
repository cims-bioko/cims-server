package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.FormSubmission;

import java.util.List;

public interface FormProcessorService {
    List<String> getBindings(String campaignUuid);
    void process(FormSubmission submission);
}
