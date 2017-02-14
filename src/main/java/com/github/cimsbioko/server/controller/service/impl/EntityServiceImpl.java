package com.github.cimsbioko.server.controller.service.impl;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import com.github.cimsbioko.server.controller.service.EntityService;
import com.github.cimsbioko.server.controller.service.refactor.crudhelpers.AbstractEntityCrudHelperImpl;
import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.domain.model.User;
import com.github.cimsbioko.server.domain.util.CalendarUtil;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.service.CurrentUser;
import com.github.cimsbioko.server.controller.service.EntityValidationService;
import com.github.cimsbioko.server.domain.model.AuditableCollectedEntity;
import com.github.cimsbioko.server.domain.model.AuditableEntity;
import com.github.cimsbioko.server.domain.service.SitePropertiesService;
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
    private CurrentUser currentUser;
    private CalendarUtil calendarUtil;
    private SitePropertiesService siteProperties;
    private EntityValidationService classValidator;

    public EntityServiceImpl(GenericDao genericDao, CurrentUser currentUser, CalendarUtil calendarUtil, SitePropertiesService siteProperties, EntityValidationService classValidator) {
        this.genericDao = genericDao;
        this.currentUser = currentUser;
        this.calendarUtil = calendarUtil;
        this.siteProperties = siteProperties;
        this.classValidator = classValidator;
    }

    @Transactional
    public <T> void create(T entityItem) throws IllegalArgumentException, ConstraintViolations, SQLException {
        if (entityItem instanceof AuditableEntity) {

            try {
                AbstractEntityCrudHelperImpl.setEntityUuidIfNull((AuditableEntity) entityItem);
            } catch (Exception e) {
                System.out.println(e.toString());
            }


            try {
                Calendar insertDate = calendarUtil.convertDateToCalendar(new Date());
                ((AuditableEntity) entityItem).setInsertDate(insertDate);
            } catch (Exception e) {
                System.out.println(e.toString());
            }


            try {
                if (currentUser != null) {
                    ((AuditableEntity) entityItem).setInsertBy(currentUser.getCurrentUser());
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        setStatusPending(entityItem);
        classValidator.validateEntity(entityItem);

        genericDao.create(entityItem);
    }

    @Transactional
    public <T> void delete(T persistentObject) throws SQLException {
        Method setDeletedMethod;
        if (persistentObject instanceof AuditableEntity) {
            Method voidedByMethod;
            Method voidedDateMethod;

            try {
                voidedByMethod = persistentObject.getClass().getMethod("setVoidBy", User.class);
                voidedByMethod.invoke(persistentObject, currentUser.getCurrentUser());

                Calendar voidDate = calendarUtil.convertDateToCalendar(new Date());
                voidedDateMethod = persistentObject.getClass().getMethod("setVoidDate", Calendar.class);
                voidedDateMethod.invoke(persistentObject, voidDate);

                setDeletedMethod = persistentObject.getClass().getMethod("setDeleted", boolean.class);
                setDeletedMethod.invoke(persistentObject, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            setStatusVoided(persistentObject);
            genericDao.update(persistentObject);
        } else if (persistentObject instanceof User) {
            try {
                setDeletedMethod = persistentObject.getClass().getMethod("setDeleted", boolean.class);
                setDeletedMethod.invoke(persistentObject, true);
                genericDao.update(persistentObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            genericDao.delete(persistentObject);
        }
    }

    @Transactional
    public <T> void save(T entityItem) throws ConstraintViolations, SQLException {
        setStatusPending(entityItem);
        classValidator.validateEntity(entityItem);
        genericDao.update(genericDao.merge(entityItem));
    }

    public <T> T read(Class<T> entityType, String id) {
        return genericDao.read(entityType, id);
    }

    private <T> void setStatusVoided(T entityItem) {
        if (entityItem instanceof AuditableCollectedEntity) {
            ((AuditableCollectedEntity) entityItem).setStatus(siteProperties.getDataStatusVoidCode());
            ((AuditableCollectedEntity) entityItem).setStatusMessage("");
        }
    }

    private <T> void setStatusPending(T entityItem) {
        if (entityItem instanceof AuditableCollectedEntity) {
            ((AuditableCollectedEntity) entityItem).setStatus(siteProperties.getDataStatusPendingCode());
            ((AuditableCollectedEntity) entityItem).setStatusMessage("");
        }
    }
}