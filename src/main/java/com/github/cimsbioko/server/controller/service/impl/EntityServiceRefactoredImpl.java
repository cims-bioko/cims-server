package com.github.cimsbioko.server.controller.service.impl;

import com.github.cimsbioko.server.controller.service.EntityServiceRefactored;
import com.github.cimsbioko.server.controller.service.refactor.crudhelpers.AbstractEntityCrudHelperImpl;
import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.service.CurrentUser;
import com.github.cimsbioko.server.controller.service.EntityValidationService;
import com.github.cimsbioko.server.domain.model.AuditableEntity;
import com.github.cimsbioko.server.domain.service.SitePropertiesService;
import com.github.cimsbioko.server.domain.util.CalendarUtil;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

public abstract class EntityServiceRefactoredImpl implements EntityServiceRefactored {

    private GenericDao genericDao;
    private CurrentUser currentUser;
    private CalendarUtil calendarUtil;
    private SitePropertiesService sitePropertiesService;
    private EntityValidationService entityValidationService;

    public EntityServiceRefactoredImpl(GenericDao genericDao, CurrentUser currentUser, CalendarUtil calendarUtil,
                                       SitePropertiesService sitePropertiesService, EntityValidationService entityValidationService) {
        this.genericDao = genericDao;
        this.currentUser = currentUser;
        this.calendarUtil = calendarUtil;
        this.sitePropertiesService = sitePropertiesService;
        this.entityValidationService = entityValidationService;
    }

    @Transactional
    public void create(AuditableEntity entityItem) throws ConstraintViolations {

        if (null != currentUser) {
            entityItem.setInsertBy(currentUser.getCurrentUser());
        }

        Calendar insertDate = calendarUtil.dateToCalendar(new Date());
        entityItem.setInsertDate(insertDate);

        AbstractEntityCrudHelperImpl.setEntityUuidIfNull(entityItem);

        entityValidationService.setStatusPending(entityItem);

        entityValidationService.validateEntity(entityItem);

        genericDao.create(entityItem);

    }

    @Transactional
    public void save(AuditableEntity entityItem) throws ConstraintViolations {
        entityValidationService.setStatusPending(entityItem);
        entityValidationService.validateEntity(entityItem);
        genericDao.update(genericDao.merge(entityItem));
    }

    public <T> T read(Class<T> entityType, String id) {
        return genericDao.read(entityType, id);
    }

    @Transactional
    public void delete(AuditableEntity entityItem) throws IllegalArgumentException {

        Calendar voidDate = calendarUtil.dateToCalendar(new Date());
        entityItem.setVoidDate(voidDate);
        entityItem.setVoidBy(currentUser.getCurrentUser());
        entityItem.setDeleted(true);

        entityValidationService.setStatusVoided(entityItem);

        genericDao.update(entityItem);

    }

}
