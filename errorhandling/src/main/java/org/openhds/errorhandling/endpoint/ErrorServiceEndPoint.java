package org.openhds.errorhandling.endpoint;

import org.openhds.domain.model.ErrorLog;

public interface ErrorServiceEndPoint {

    void logError(ErrorLog errorLog);

}
