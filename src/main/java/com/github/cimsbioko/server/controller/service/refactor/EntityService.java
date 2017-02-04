package com.github.cimsbioko.server.controller.service.refactor;


import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.AuditableEntity;

import java.util.List;

import static com.github.cimsbioko.server.domain.model.PrivilegeConstants.*;

public interface EntityService<T extends AuditableEntity> {

    @Authorized({VIEW_ENTITY})
    List<T> getAll();

    @Authorized({VIEW_ENTITY})
    T getByExtId(String id);

    @Authorized({VIEW_ENTITY})
    T getByUuid(String id);

    @Authorized({VIEW_ENTITY})
    boolean isEligibleForCreation(T entity, ConstraintViolations cv);

    @Authorized({DELETE_ENTITY})
    void delete(T entity) throws IllegalArgumentException;

    @Authorized({CREATE_ENTITY})
    void create(T entity) throws ConstraintViolations;

    @Authorized({EDIT_ENTITY})
    void save(T entity) throws ConstraintViolations;

}
