package com.github.cimsbioko.server.domain.constraint.impl;

import javax.validation.ConstraintValidator;

import com.github.cimsbioko.server.domain.constraint.CheckIndividualAge;
import com.github.cimsbioko.server.domain.constraint.CheckRelationshipAge;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.service.impl.SitePropertiesServiceImpl;

public class CheckRelationshipAgeImpl extends CheckIndividualAge implements ConstraintValidator<CheckRelationshipAge, Individual> {

    public void initialize(CheckRelationshipAge arg0) {
        SitePropertiesServiceImpl properties = (SitePropertiesServiceImpl) context.getBean("siteProperties");
        requiredAge = properties.getMinimumAgeOfMarriage();
    }
}
