package com.github.cimsbioko.server.dao.service.impl;

import java.io.Serializable;
import java.util.List;

import com.github.cimsbioko.server.dao.service.Dao;
import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.domain.model.LocationHierarchy;
import com.github.cimsbioko.server.domain.model.User;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import com.github.cimsbioko.server.domain.model.AuditableEntity;

@SuppressWarnings("unchecked")
public class BaseDaoImpl<T, PK extends Serializable> implements Dao<T, PK> {

    /**
     * Model object type
     */
    Class<T> entityType;
    /**
     * Hibernate session factory configured in applicationContext
     */
    SessionFactory sessFact;

    public BaseDaoImpl(Class<T> entityType) {
        if (entityType == null) {
            throw new IllegalArgumentException(
                    "entity type not specified for dao");
        }
        this.entityType = entityType;
    }

    /**
     * Persist the <code>newInstance</code> object into the database
     */
    public PK save(T newInstance) {
        getSession().flush();
        return (PK) getSession().save(newInstance);
    }

    public PK create(T newInstance) {
        getSession().flush();
        return (PK) getSession().save(newInstance);
    }

    public <S> S merge(S newInstance) {
        return (S) getSession().merge(newInstance);

    }

    public void saveOrUpdate(T newInstance) {
        getSession().saveOrUpdate(newInstance);
    }

    /**
     * Retrieve an object that was previously persisted to the
     * database using the indicated <code>id</code> as primary key
     */
    public T read(PK id) {
        return (T) getSession().get(entityType, id);
    }

    /**
     * Save changes made to a persistent object
     */
    public void update(T transientObject) {
        getSession().update(transientObject);
    }

    /**
     * Remove an object from persistent storage in the database
     */
    public void delete(T persistentObject) {
        getSession().delete(persistentObject);
    }

    public void evict(T persistentObject) {
        getSession().evict(persistentObject);
    }

    /**
     * Retrieve an entity with the specified <code>propertyName</code>
     * with the associated <code>value</code>
     */
    public T findByProperty(String propertyName, Object value) {
        Criteria criteria = getSession().createCriteria(entityType).add(Restrictions.eq(propertyName, value));
        if (AuditableEntity.class.isAssignableFrom(entityType)) {
            criteria = addImplicitRestrictions(criteria);
        }
        return (T) criteria.uniqueResult();
    }

    /**
     * Retrieve a list of all objects of the DAO's <code>entityType</code>
     */
    public List<T> findAll(boolean filterDeleted) {
        Criteria criteria = getSession().createCriteria(entityType);
        if (AuditableEntity.class.isAssignableFrom(entityType) ||
                LocationHierarchy.class.isAssignableFrom(entityType) ||
                FieldWorker.class.isAssignableFrom(entityType)) {
            criteria = addImplicitRestrictions(criteria);
        }
        if (filterDeleted) {
            criteria = criteria.add(Restrictions.eq("deleted", false));
        }
        return criteria.list();
    }

    /**
     * Retrieve a list of all objects of the DAO's <code>entityType</code>
     * and also limit the number or results returned specified by the index of
     * <code>firstResult</code> and the amount of <code>maxResults</code>
     */
    public List<T> findPaged(int maxResults, int firstResult) {
        Criteria criteria = getSession().createCriteria(entityType);
        if (AuditableEntity.class.isAssignableFrom(entityType) ||
                LocationHierarchy.class.isAssignableFrom(entityType) ||
                FieldWorker.class.isAssignableFrom(entityType) ||
                User.class.isAssignableFrom(entityType)) {
            criteria = addImplicitRestrictions(criteria);
        }
        return criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).
                setFirstResult(firstResult).setMaxResults(maxResults).
                list();
    }

    /**
     * Retrieve the total number of unique entities in the database
     */
    public long getTotalCount() {
        Criteria criteria = getSession().createCriteria(entityType);
        if (AuditableEntity.class.isAssignableFrom(entityType) ||
                LocationHierarchy.class.isAssignableFrom(entityType) ||
                FieldWorker.class.isAssignableFrom(entityType) ||
                User.class.isAssignableFrom(entityType)) {
            criteria = addImplicitRestrictions(criteria);
        }

        return (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
    }

    public List<T> findListByProperty(String propertyName, Object value, boolean filterDeleted) {
        Criteria criteria = getSession().createCriteria(entityType).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).
                add(Restrictions.eq(propertyName, value));
        if (filterDeleted) {
            criteria = criteria.add(Restrictions.eq("deleted", false));
        }
        return criteria.list();
    }

    public List<T> findListByPropertyPaged(String propertyName, Object value, int firstResult, int maxResults) {
        Criteria criteria = getSession().createCriteria(entityType).add(Restrictions.eq(propertyName, value));
        if (AuditableEntity.class.isAssignableFrom(entityType)) {
            criteria = addImplicitRestrictions(criteria);
        }
        return (List<T>) criteria.setFirstResult(firstResult).setMaxResults(maxResults).
                setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
    }

    public List<T> findListByPropertyPagedInsensitive(String propertyName, Object value, int firstResult, int maxResults) {
        Criteria criteria = getSession().createCriteria(entityType).add(Restrictions.ilike(propertyName, (String) value, MatchMode.ANYWHERE));
        if (AuditableEntity.class.isAssignableFrom(entityType) || User.class.isAssignableFrom(entityType)) {
            criteria = addImplicitRestrictions(criteria);
        }
        return (List<T>) criteria.setFirstResult(firstResult).setMaxResults(maxResults).
                setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
    }

    public List<T> findByExample(T exampleInstance, String... excludeProperty) {
        Criteria crit = getSession().createCriteria(entityType);
        Example example = Example.create(exampleInstance);
        for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        crit.add(example);
        return crit.list();
    }

    public long getCountByProperty(String propertyName, Object value) {
        Criteria criteria = getSession().createCriteria(entityType).add(Restrictions.eq(propertyName, value));
        if (AuditableEntity.class.isAssignableFrom(entityType)) {
            criteria = addImplicitRestrictions(criteria);
        }
        return (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
    }

    protected Criteria addImplicitRestrictions(Criteria criteria) {
        return criteria.add(Restrictions.eq("deleted", false));
    }

    public void setSessionFactory(SessionFactory sessFact) {
        this.sessFact = sessFact;
    }

    protected Session getSession() {
        return sessFact.getCurrentSession();
    }

}
