package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.Error;

public interface ErrorService {
    Error logError(Error error);
}
