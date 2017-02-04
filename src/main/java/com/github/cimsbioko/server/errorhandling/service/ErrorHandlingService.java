package com.github.cimsbioko.server.errorhandling.service;

import com.github.cimsbioko.server.domain.model.ErrorLog;

public interface ErrorHandlingService {

    ErrorLog logError(ErrorLog error);

}
