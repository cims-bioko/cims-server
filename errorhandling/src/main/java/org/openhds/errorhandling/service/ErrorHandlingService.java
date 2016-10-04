package org.openhds.errorhandling.service;

import org.openhds.domain.model.ErrorLog;

public interface ErrorHandlingService {

    ErrorLog logError(ErrorLog error);

}
