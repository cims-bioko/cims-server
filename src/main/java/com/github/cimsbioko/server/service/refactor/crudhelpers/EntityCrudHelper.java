package com.github.cimsbioko.server.service.refactor.crudhelpers;

import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.AuditableEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EntityCrudHelper<T extends AuditableEntity> {

    List<T> getAll();

    T getByExtId(String id);

    T getByUuid(String id);

    @Transactional
    void delete(T entity) throws IllegalArgumentException;

    @Transactional(rollbackFor = Exception.class)
    void create(T entity) throws ConstraintViolations;

    @Transactional
    void save(T entity) throws ConstraintViolations;

}
