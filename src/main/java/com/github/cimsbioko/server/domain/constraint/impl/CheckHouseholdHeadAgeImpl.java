package com.github.cimsbioko.server.domain.constraint.impl;

import javax.validation.ConstraintValidator;

import com.github.cimsbioko.server.domain.service.impl.SitePropertiesServiceImpl;
import com.github.cimsbioko.server.domain.constraint.CheckHouseholdHeadAge;
import com.github.cimsbioko.server.domain.constraint.CheckIndividualAge;
import com.github.cimsbioko.server.domain.model.Individual;

public class CheckHouseholdHeadAgeImpl extends CheckIndividualAge implements ConstraintValidator<CheckHouseholdHeadAge, Individual> {

    public void initialize(CheckHouseholdHeadAge arg0) {
        SitePropertiesServiceImpl properties = (SitePropertiesServiceImpl) context.getBean("siteProperties");
        requiredAge = properties.getMinimumAgeOfHouseholdHead();
        this.allowNull = arg0.allowNull();
    }
}
