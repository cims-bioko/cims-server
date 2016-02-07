package org.openhds.controller.service.impl;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.CurrentUser;
import org.openhds.controller.service.EntityServiceRefactored;
import org.openhds.controller.service.EntityValidationService;
import org.openhds.controller.service.refactor.crudhelpers.AbstractEntityCrudHelperImpl;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.AuditableEntity;
import org.openhds.domain.service.SitePropertiesService;
import org.openhds.domain.util.CalendarUtil;
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

        Calendar insertDate = calendarUtil.convertDateToCalendar(new Date());
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
        genericDao.update( genericDao.merge(entityItem) );
    }

    public <T> T read(Class<T> entityType, String id) {
        return genericDao.read(entityType, id);
    }

    @Transactional
    public void delete(AuditableEntity entityItem) throws IllegalArgumentException {

        Calendar voidDate = calendarUtil.convertDateToCalendar(new Date());
        entityItem.setVoidDate(voidDate);
        entityItem.setVoidBy(currentUser.getCurrentUser());
        entityItem.setDeleted(true);

        entityValidationService.setStatusVoided(entityItem);

        genericDao.update(entityItem);

    }

}
