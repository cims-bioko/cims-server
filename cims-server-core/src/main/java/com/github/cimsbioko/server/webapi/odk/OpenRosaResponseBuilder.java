package com.github.cimsbioko.server.webapi.odk;

import com.github.cimsbioko.server.domain.FormSubmission;

public interface OpenRosaResponseBuilder {
    String response(String message);
    String submissionResponse(FormSubmission fs);
}
