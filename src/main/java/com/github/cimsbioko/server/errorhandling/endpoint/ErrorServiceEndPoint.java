package com.github.cimsbioko.server.errorhandling.endpoint;

import com.github.cimsbioko.server.domain.model.ErrorLog;

public interface ErrorServiceEndPoint {

    void logError(ErrorLog errorLog);

}
