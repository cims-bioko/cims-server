package com.github.cimsbioko.server.errorhandling.dao;

import com.github.cimsbioko.server.domain.model.Error;

public interface ErrorDao {

    Error createError(Error error);

}
