package com.github.cimsbioko.server.domain.constraint.impl;

import javax.validation.ConstraintValidator;

import com.github.cimsbioko.server.domain.constraint.CheckIndividualParentAge;
import com.github.cimsbioko.server.domain.service.impl.SitePropertiesServiceImpl;
import com.github.cimsbioko.server.domain.constraint.CheckIndividualAge;
import com.github.cimsbioko.server.domain.model.Individual;

public class CheckIndividualParentAgeImpl extends CheckIndividualAge implements ConstraintValidator<CheckIndividualParentAge, Individual> {

    public void initialize(CheckIndividualParentAge arg0) {
        SitePropertiesServiceImpl properties = (SitePropertiesServiceImpl) context.getBean("siteProperties");
        requiredAge = properties.getMinimumAgeOfParents();
    }
}
