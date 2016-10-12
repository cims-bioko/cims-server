package org.openhds.errorhandling.dao;

import org.openhds.domain.model.ErrorLog;

public interface ErrorLogDAO {

    ErrorLog createErrorLog(ErrorLog error);

}
