package com.github.cimsbioko.server.service.refactor;


import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.AuditableEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface EntityService<T extends AuditableEntity> {

    @PreAuthorize("hasAuthority('VIEW_ENTITY')")
    List<T> getAll();

    @PreAuthorize("hasAuthority('VIEW_ENTITY')")
    T getByExtId(String id);

    @PreAuthorize("hasAuthority('VIEW_ENTITY')")
    T getByUuid(String id);

    @PreAuthorize("hasAuthority('VIEW_ENTITY')")
    boolean isEligibleForCreation(T entity, ConstraintViolations cv);

    @PreAuthorize("hasAuthority('DELETE_ENTITY')")
    void delete(T entity) throws IllegalArgumentException;

    @PreAuthorize("hasAuthority('CREATE_ENTITY')")
    void create(T entity) throws ConstraintViolations;

    @PreAuthorize("hasAuthority('EDIT_ENTITY')")
    void save(T entity) throws ConstraintViolations;

}
