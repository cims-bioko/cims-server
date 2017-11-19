package com.github.cimsbioko.server.controller.service;

import com.github.cimsbioko.server.domain.model.Error;

public interface ErrorService {
    Error logError(Error error);
}
