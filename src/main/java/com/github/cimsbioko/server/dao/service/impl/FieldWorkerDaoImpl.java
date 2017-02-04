package com.github.cimsbioko.server.dao.service.impl;

import com.github.cimsbioko.server.domain.model.FieldWorker;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import com.github.cimsbioko.server.domain.service.SitePropertiesService;

public class FieldWorkerDaoImpl extends BaseDaoImpl<FieldWorker, String> {

    SitePropertiesService properties;

    public FieldWorkerDaoImpl(Class<FieldWorker> entityType) {
        super(entityType);
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
