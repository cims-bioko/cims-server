package com.github.cimsbioko.server.errorhandling;

import com.github.cimsbioko.server.domain.model.Error;

public interface ErrorEndPoint {

    void logError(Error error);

}
