package com.github.cimsbioko.server.dao;

import java.util.List;

import org.hibernate.Session;

public interface GenericDao {

    interface OrderProperty {

        String getPropertyName();

        boolean isAscending();
    }

    <T> String create(T newInstance);

    <T> T read(Class<T> entityType, String id);

    <T> void update(T transientObject);

    <T> void delete(T persistentObject);

    <T> T merge(T entityItem);

    <T> List<T> findAll(Class<T> entityType, boolean filterDeleted);

    <T> List<T> findAllDistinct(Class<T> entityType);

    <T> List<T> findAllWithOrder(Class<T> entityType, OrderProperty... orderProps);

    /**
     * Find an entry of some type from the database. Does not exclude "deleted" entries.
     *
     * @param entityType   The class of the entry we're looking for (e.g. Individual.class)
     * @param propertyName The column name to filter by (e.g. "extId")
     * @param value        The value to match in this column (e.g. an extId)
     * @return an object of the class type passed
     */
    <T> T findByProperty(Class<T> entityType, String propertyName, Object value);

    /**
     * Find an entry of some type from the database.
     *
     * @param entityType    The class of the entry we're looking for (e.g. Individual.class)
     * @param propertyName  The column name to filter by (e.g. "extId")
     * @param value         The value to match in this column (e.g. an extId)
     * @param filterDeleted True to only return entries which haven't been marked deleted
     * @return an object of the class type passed
     */
    <T> T findByProperty(Class<T> entityType, String propertyName, Object value, boolean filterDeleted);

    <T> List<T> findListByProperty(Class<T> entityType, String propertyName, Object value);

    <T> List<T> findListByProperty(Class<T> entityType, String propertyName, Object value, boolean filterDeleted);

    void clear();

    Session getSession();

}
