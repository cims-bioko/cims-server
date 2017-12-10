package com.github.cimsbioko.server.controller.service.impl;

import com.github.cimsbioko.server.controller.service.EntityServiceRefactored;
import com.github.cimsbioko.server.controller.service.refactor.crudhelpers.AbstractEntityCrudHelperImpl;
import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.service.EntityValidationService;
import com.github.cimsbioko.server.domain.model.AuditableEntity;
import com.github.cimsbioko.server.domain.util.CalendarUtil;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

public abstract class EntityServiceRefactoredImpl implements EntityServiceRefactored {

    private GenericDao genericDao;
    private CalendarUtil calendarUtil;
    private EntityValidationService entityValidationService;

    public EntityServiceRefactoredImpl(GenericDao genericDao, CalendarUtil calendarUtil,
                                       EntityValidationService entityValidationService) {
        this.genericDao = genericDao;
        this.calendarUtil = calendarUtil;
        this.entityValidationService = entityValidationService;
    }

    @Transactional
    public void create(AuditableEntity entityItem) throws ConstraintViolations {
        Calendar created = calendarUtil.dateToCalendar(new Date());
        entityItem.setCreated(created);
        AbstractEntityCrudHelperImpl.setEntityUuidIfNull(entityItem);
        entityValidationService.validateEntity(entityItem);
        genericDao.create(entityItem);
    }

    @Transactional
    public void save(AuditableEntity entityItem) throws ConstraintViolations {
        entityValidationService.validateEntity(entityItem);
        genericDao.update(genericDao.merge(entityItem));
    }

    public <T> T read(Class<T> entityType, String id) {
        return genericDao.read(entityType, id);
    }

    @Transactional
    public void delete(AuditableEntity entityItem) throws IllegalArgumentException {
        entityItem.setDeleted(true);
        genericDao.update(entityItem);
    }

}
