package com.github.cimsbioko.server.controller.service.refactor;


import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.AuditableEntity;
import com.github.cimsbioko.server.domain.model.Privilege;

import java.util.List;

public interface EntityService<T extends AuditableEntity> {

    @Authorized({Privilege.VIEW_ENTITY})
    List<T> getAll();

    @Authorized({Privilege.VIEW_ENTITY})
    T getByExtId(String id);

    @Authorized({Privilege.VIEW_ENTITY})
    T getByUuid(String id);

    @Authorized({Privilege.VIEW_ENTITY})
    boolean isEligibleForCreation(T entity, ConstraintViolations cv);

    @Authorized({Privilege.DELETE_ENTITY})
    void delete(T entity) throws IllegalArgumentException;

    @Authorized({Privilege.CREATE_ENTITY})
    void create(T entity) throws ConstraintViolations;

    @Authorized({Privilege.EDIT_ENTITY})
    void save(T entity) throws ConstraintViolations;

}
