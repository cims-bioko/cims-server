package com.github.cimsbioko.server.controller.service;

import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.AuditableEntity;

public interface EntityServiceRefactored {

    <T> T read(Class<T> entityType, String id);

    void delete(AuditableEntity auditableEntity) throws IllegalArgumentException;

    void create(AuditableEntity auditableEntity) throws ConstraintViolations;

    void save(AuditableEntity auditableEntity) throws ConstraintViolations;

}
