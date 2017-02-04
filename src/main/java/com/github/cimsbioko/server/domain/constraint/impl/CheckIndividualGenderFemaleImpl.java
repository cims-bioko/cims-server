package com.github.cimsbioko.server.domain.constraint.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.github.cimsbioko.server.domain.service.impl.SitePropertiesServiceImpl;
import com.github.cimsbioko.server.domain.constraint.AppContextAware;
import com.github.cimsbioko.server.domain.constraint.CheckIndividualGenderFemale;
import com.github.cimsbioko.server.domain.model.Individual;

public class CheckIndividualGenderFemaleImpl extends AppContextAware implements
        ConstraintValidator<CheckIndividualGenderFemale, Individual> {

    private boolean allowNull;
    private SitePropertiesServiceImpl properties;

    public void initialize(CheckIndividualGenderFemale arg0) {
        properties = (SitePropertiesServiceImpl) context.getBean("siteProperties");
        this.allowNull = arg0.allowNull();
    }

    public boolean isValid(Individual individual, ConstraintValidatorContext context) {

        if (individual == null) {
            return allowNull;
        }

        if (individual.getExtId().equals(properties.getUnknownIdentifier()))
            return true;

        return individual.getGender().equals(properties.getFemaleCode());

    }
}