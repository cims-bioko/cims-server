package com.github.cimsbioko.server.dao.service.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import com.github.cimsbioko.server.domain.model.AuditableEntity;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.service.SitePropertiesService;

/**
 * A specialized class for the Individual entity
 * This was introduced because Individual has to filter out the Unknown Individual
 * whenever it makes a query for searching and/or paging results
 *
 * @author Dave Roberge
 */
public class IndividualDaoImpl extends BaseDaoImpl<Individual, String> {

    SitePropertiesService properties;

    public IndividualDaoImpl(Class<Individual> entityType) {
        super(entityType);
    }

    @Override
    public Individual findByProperty(String propertyName, Object value) {
        // this method is invoked when looking up an individual by external id
        // if the property is extId, we want to make sure to INCLUDE the UNK individual
        // because the user may want to select the UNK individual
        // Otherwise we want to ignore it
        if (propertyName.toLowerCase().equals("extid")) {
            Criteria criteria = getSession().createCriteria(entityType).add(Restrictions.eq(propertyName, value));
            if (AuditableEntity.class.isAssignableFrom(entityType)) {
                criteria = super.addImplicitRestrictions(criteria);
            }
            return (Individual) criteria.uniqueResult();
        }
        return super.findByProperty(propertyName, value);
    }

    @Override
    protected Criteria addImplicitRestrictions(Criteria criteria) {
        criteria = super.addImplicitRestrictions(criteria);
        return criteria.add(Restrictions.ne("extId", properties.getUnknownIdentifier()));
    }

    public SitePropertiesService getProperties() {
        return properties;
    }

    public void setProperties(SitePropertiesService properties) {
        this.properties = properties;
    }
}
