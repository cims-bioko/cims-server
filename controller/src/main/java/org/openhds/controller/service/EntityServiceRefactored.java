package org.openhds.controller.service;


import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.domain.model.AuditableEntity;

public interface EntityServiceRefactored {

    public <T> T read(Class<T> entityType, String id);

    public void delete(AuditableEntity auditableEntity) throws IllegalArgumentException;

    public void create(AuditableEntity auditableEntity) throws ConstraintViolations;

    public void save(AuditableEntity auditableEntity) throws ConstraintViolations;


}
