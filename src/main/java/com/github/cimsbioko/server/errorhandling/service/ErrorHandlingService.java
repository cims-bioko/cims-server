package com.github.cimsbioko.server.errorhandling.service;

import com.github.cimsbioko.server.domain.model.Error;

public interface ErrorHandlingService {

    Error logError(Error error);

}
