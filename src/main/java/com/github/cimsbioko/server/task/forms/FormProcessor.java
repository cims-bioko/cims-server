package com.github.cimsbioko.server.task.forms;

import com.github.cimsbioko.server.controller.service.FormSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;

public interface FormProcessor {
    void setFormService(FormSubmissionService formService);
    int processForms();
}
