package com.github.cimsbioko.server.service;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Optional;

public interface EnketoService {
    Optional<URI> editSubmission(HttpServletRequest req, String instanceId);
}
