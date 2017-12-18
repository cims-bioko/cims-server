package com.github.cimsbioko.server.domain.service;

import org.jdom2.Element;

public interface ValueConstraintService {

    boolean isValidConstraintValue(String constraintName, Object value);

}
