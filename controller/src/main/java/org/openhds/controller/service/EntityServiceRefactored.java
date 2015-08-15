package org.openhds.controller.service;


import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.domain.model.AuditableEntity;

public interface EntityServiceRefactored {

    <T> T read(Class<T> entityType, String id);

    void delete(AuditableEntity auditableEntity) throws IllegalArgumentException;

    void create(AuditableEntity auditableEntity) throws ConstraintViolations;

    void save(AuditableEntity auditableEntity) throws ConstraintViolations;


}
