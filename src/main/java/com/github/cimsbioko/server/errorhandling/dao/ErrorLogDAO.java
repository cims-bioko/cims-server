package com.github.cimsbioko.server.errorhandling.dao;

import com.github.cimsbioko.server.domain.model.ErrorLog;

public interface ErrorLogDAO {

    ErrorLog createErrorLog(ErrorLog error);

}
