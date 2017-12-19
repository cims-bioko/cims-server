package com.github.cimsbioko.server.service.impl;

import java.util.Calendar;
import java.util.Date;

import com.github.cimsbioko.server.service.EntityService;
import com.github.cimsbioko.server.service.refactor.crudhelpers.AbstractEntityCrudHelperImpl;
import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.domain.model.User;
import com.github.cimsbioko.server.domain.util.CalendarUtil;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.EntityValidationService;
import com.github.cimsbioko.server.domain.model.AuditableEntity;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base Entity Service Class Implementation
 * This class is meant to be a generic implementation that can be used by all entities
 * It also aims to provide "hooks" so that in specialized cases, business logic can be added
 * to a transaction. For example, if something needs to be checked before the transaction, a subclass
 * could override the onBeforeCommit method and add the necessary logic before the commit happens
 *
 * @author Dave
 */
@SuppressWarnings("unchecked")
public class EntityServiceImpl implements EntityService {

    private GenericDao genericDao;
    private CalendarUtil calendarUtil;
    private EntityValidationService classValidator;

    public EntityServiceImpl(GenericDao genericDao, CalendarUtil calendarUtil, EntityValidationService classValidator) {
        this.genericDao = genericDao;
        this.calendarUtil = calendarUtil;
        this.classValidator = classValidator;
    }

    @Transactional
    public <T> void create(T entityItem) throws IllegalArgumentException, ConstraintViolations {
        if (entityItem instanceof AuditableEntity) {
            try {
                AbstractEntityCrudHelperImpl.setEntityUuidIfNull((AuditableEntity) entityItem);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            try {
                Calendar created = calendarUtil.dateToCalendar(new Date());
                ((AuditableEntity) entityItem).setCreated(created);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        classValidator.validateEntity(entityItem);
        genericDao.create(entityItem);
    }

    @Transactional
    public <T> void delete(T persistentObject) {
        if (persistentObject instanceof AuditableEntity) {
            AuditableEntity auditable = (AuditableEntity)persistentObject;
            auditable.setDeleted(true);
            genericDao.update(auditable);
        } else if (persistentObject instanceof User) {
            User user = (User)persistentObject;
            user.setDeleted(true);
            genericDao.update(user);
        } else {
            genericDao.delete(persistentObject);
        }
    }

    @Transactional
    public <T> void save(T entityItem) throws ConstraintViolations {
        classValidator.validateEntity(entityItem);
        genericDao.update(genericDao.merge(entityItem));
    }

    public <T> T read(Class<T> entityType, String id) {
        return genericDao.read(entityType, id);
    }
}