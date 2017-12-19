package com.github.cimsbioko.server.service;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Interface that represents a generic service that can be used to create/read/delete entities in the system
 * This serves as a basic entity service that can be used by cruds
 * In the case of specialized functionality for an entity, a new service class can be created
 * That service class can inherit from this one and be injected in through the Spring application context
 * or can be a completely different service
 *
 * @author Dave
 */
public interface EntityService {

    /**
     * Creates/persists an entity to the datastore
     *
     * @param entityItem the item to persist
     * @throws IllegalArgumentException
     * @throws ConstraintViolationException if the entity violates a constraint
     * @throws SQLException                 if the entity fails to be persisted
     */
    @PreAuthorize("hasAuthority('CREATE_ENTITY')")
    <T> void create(T entityItem) throws IllegalArgumentException, ConstraintViolations, SQLException;

    @PreAuthorize("hasAuthority('EDIT_ENTITY')")
    <T> void save(T entityItem) throws ConstraintViolations, SQLException;

    /**
     * Read entity
     *
     * @param id
     * @return
     */
    @PreAuthorize("hasAuthority('VIEW_ENTITY')")
    <T> T read(Class<T> entityType, String id);

    /**
     * Delete entity from persistence
     *
     * @param entityItem
     * @throws SQLException
     */
    @PreAuthorize("hasAuthority('DELETE_ENTITY')")
    <T> void delete(T entityItem) throws SQLException;

}
