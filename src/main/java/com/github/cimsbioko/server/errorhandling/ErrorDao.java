package com.github.cimsbioko.server.errorhandling;

import com.github.cimsbioko.server.domain.model.Error;

public interface ErrorDao {

    Error save(Error error);

}
