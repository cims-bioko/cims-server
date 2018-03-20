package com.github.cimsbioko.server.dao.impl;

import com.github.cimsbioko.server.dao.Dao;
import com.github.cimsbioko.server.domain.AuditableEntity;
import com.github.cimsbioko.server.domain.FieldWorker;
import com.github.cimsbioko.server.domain.LocationHierarchy;
import com.github.cimsbioko.server.domain.User;
import org.apache.lucene.search.Query;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class BaseDaoImpl<T, PK extends Serializable> implements Dao<T, PK> {

    Class<T> entityType;

    String [] searchFields;

    @Autowired
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
            criteria = criteria.add(Restrictions.isNull("deleted"));
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

    protected Criteria addImplicitRestrictions(Criteria criteria) {
        return criteria.add(Restrictions.isNull("deleted"));
    }

    public void setSessionFactory(SessionFactory sessFact) {
        this.sessFact = sessFact;
    }

    protected Session getSession() {
        return sessFact.getCurrentSession();
    }

    protected FullTextSession getFullTextSession() {
        return Search.getFullTextSession(getSession());
    }

    protected SearchFactory getSearchFactory() {
        return getFullTextSession().getSearchFactory();
    }

    protected QueryBuilder getSearchQueryBuilder() {
        return getSearchFactory().buildQueryBuilder().forEntity(entityType).get();
    }

    protected String[] getSearchFields() {
        if (searchFields == null) {
            List<String> fieldNames = new ArrayList<>();
            for (Field field : entityType.getDeclaredFields()) {
                if (field.isAnnotationPresent(org.hibernate.search.annotations.Field.class)) {
                    fieldNames.add(field.getName());
                }
            }
            searchFields = fieldNames.toArray(new String[]{});
        }
        return searchFields;
    }

    protected Query getSearchQuery(String query) {
        return getSearchQueryBuilder()
                .keyword()
                .fuzzy()
                .onFields(getSearchFields())
                .matching(query)
                .createQuery();
    }

    protected FullTextQuery getEntitySearchQuery(String query) {
        return getFullTextSession()
                .createFullTextQuery(getSearchQuery(query), entityType);
    }

    public long getSearchCount(String query) {
        return getEntitySearchQuery(query).getResultSize();
    }

    public List<T> findBySearch(String query, int first, int max) {
        return getEntitySearchQuery(query)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList();
    }
}
