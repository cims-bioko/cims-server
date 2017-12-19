package com.github.cimsbioko.server.service.refactor.crudhelpers;

import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.idgen.UUIDGenerator;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.CurrentUser;
import com.github.cimsbioko.server.service.EntityValidationService;
import com.github.cimsbioko.server.domain.model.AuditableEntity;
import com.github.cimsbioko.server.service.SitePropertiesService;
import com.github.cimsbioko.server.util.CalendarUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

public abstract class AbstractEntityCrudHelperImpl<T extends AuditableEntity> implements EntityCrudHelper<T> {

    @Autowired
    protected GenericDao genericDao;
    @Autowired
    protected CurrentUser currentUser;
    @Autowired
    protected CalendarUtil calendarUtil;
    @Autowired
    protected SitePropertiesService sitePropertiesService;
    @Autowired
    protected EntityValidationService entityValidationService;

    @Transactional
    @Override
    public void delete(T entity) throws IllegalArgumentException {
        entity.setDeleted(true);
        genericDao.update(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void create(T entity) throws ConstraintViolations {

        //TODO: there are too many places that validations are happening - or they are atleast organized poorly.
        //TODO: clean up the validations into well defined blobs of logic.

        preCreateSanityChecks(entity);
        cascadeReferences(entity);
        validateReferences(entity);
        entity.setCreated(calendarUtil.dateToCalendar(new Date()));

        setEntityUuidIfNull(entity);
        entityValidationService.validateEntity(entity);
        genericDao.create(entity);
    }

    @Transactional
    @Override
    public void save(T entity) throws ConstraintViolations {
        validateReferences(entity);
        entityValidationService.validateEntity(entity);
        genericDao.update(genericDao.merge(entity));
    }

    public static void setEntityUuidIfNull(AuditableEntity entity) {
        if (null == entity.getUuid() || entity.getUuid().isEmpty() || entity.getUuid().equals("null")) {
            entity.setUuid(UUIDGenerator.generate());
        }
    }

    protected abstract void preCreateSanityChecks(T entity) throws ConstraintViolations;

    protected abstract void cascadeReferences(T entity) throws ConstraintViolations;

    protected abstract void validateReferences(T entity) throws ConstraintViolations;
}

