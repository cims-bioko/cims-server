package com.github.cimsbioko.server.dao.impl;

import com.github.cimsbioko.server.dao.GenericDao;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * A generic implementation of a Dao that simplifies Dao/BaseDaoImpl
 * This class differs from the Dao/BaseDaoImpl in that it is not have
 * generic parameters. Instead it uses generic methods so that only one instance
 * of this class needs to be created and can be shared by any entity
 *
 * @author dave
 */
@Repository("genericDao")
public class GenericDaoImpl implements GenericDao {

    @Autowired
    protected SessionFactory sessFact;

    public Session getSession() {
        return sessFact.getCurrentSession();
    }

    public <T> String create(T newInstance) {
        return (String) getSession().save(newInstance);
    }

    public <T> void delete(T persistentObject) {
        getSession().delete(persistentObject);
    }

    @SuppressWarnings("unchecked")
    public <T> T read(Class<T> entityType, String id) {
        return (T) getSession().get(entityType, id);
    }

    public <T> void update(T transientObject) {
        getSession().update(transientObject);
    }

    @SuppressWarnings("unchecked")
    public <T> T merge(T entityItem) {
        return (T) getSession().merge(entityItem);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findAll(Class<T> entityType, boolean filterDeleted) {
        Criteria criteria = getSession().createCriteria(entityType);

        if (filterDeleted) {
            criteria = criteria.add(Restrictions.eq("deleted", false));
        }
        return criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findAllDistinct(Class<T> entityType) {
        return getSession().createCriteria(entityType).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
    }

    public <T> T findByProperty(Class<T> entityType, String propertyName, Object value) {
        return findByProperty(entityType, propertyName, value, false);
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public <T> T findByProperty(Class<T> entityType, String propertyName, Object value, boolean filterDeleted) {
        Criteria criteria = getSession().createCriteria(entityType).add(Restrictions.eq(propertyName, value));

        if (filterDeleted) {
            criteria = criteria.add(Restrictions.eq("deleted", false));
        }

        return (T) criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public <T> List<T> findListByProperty(Class<T> entityType, String propertyName, Object value) {
        return (List<T>) getSession().createCriteria(entityType).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).
                add(Restrictions.eq(propertyName, value)).list();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findListByProperty(Class<T> entityType, String propertyName, Object value, boolean filterDeleted) {

        Criteria criteria = getSession().createCriteria(entityType).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).
                add(Restrictions.eq(propertyName, value));

        if (filterDeleted) {
            criteria = criteria.add(Restrictions.eq("deleted", false));
        }
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findAllWithOrder(Class<T> entityType, OrderProperty... orderProps) {
        Criteria criteria = getSession().createCriteria(entityType);
        return (List<T>) addOrderPropertiesToCriteria(criteria, orderProps).list();
    }

    private Criteria addOrderPropertiesToCriteria(Criteria criteria, OrderProperty... orderProps) {
        for (OrderProperty prop : orderProps) {
            Order order = (prop.isAscending() ? Order.asc(prop.getPropertyName()) : Order.desc(prop.getPropertyName()));
            criteria = criteria.addOrder(order);
        }
        return criteria;
    }
}
