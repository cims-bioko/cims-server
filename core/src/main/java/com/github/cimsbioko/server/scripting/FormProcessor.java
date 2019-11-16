package com.github.cimsbioko.server.scripting;

import com.github.cimsbioko.server.domain.FormSubmission;

public interface FormProcessor {
    void process(FormSubmission submission);
}
