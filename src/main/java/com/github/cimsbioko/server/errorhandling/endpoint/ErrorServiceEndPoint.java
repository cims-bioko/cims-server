package com.github.cimsbioko.server.errorhandling.endpoint;

import com.github.cimsbioko.server.domain.model.Error;

public interface ErrorServiceEndPoint {

    void logError(Error error);

}
