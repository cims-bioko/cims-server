package com.github.cimsbioko.server.webapi;

import com.github.cimsbioko.server.controller.service.FormSubmissionService;

public interface FormProcessor {
    void setFormService(FormSubmissionService formService);
    int processForms();
}
