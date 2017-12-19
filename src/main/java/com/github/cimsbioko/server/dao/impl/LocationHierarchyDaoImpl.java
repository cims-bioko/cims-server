package com.github.cimsbioko.server.dao.impl;

import com.github.cimsbioko.server.domain.LocationHierarchy;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * A specialized class for the LocationHierarchy entity
 * This was introduced because LocationHierarchy has to filter out the root
 * whenever it makes a query for searching and/or paging results
 */
@Repository("locationHierarchyDao")
public class LocationHierarchyDaoImpl extends BaseDaoImpl<LocationHierarchy, String> {

    public LocationHierarchyDaoImpl() {
        super(LocationHierarchy.class);
    }

    @Override
    protected Criteria addImplicitRestrictions(Criteria criteria) {
        return criteria.add(Restrictions.ne("extId", "HIERARCHY_ROOT"));
    }
}
