package org.openhds.domain.constraint.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.openhds.domain.constraint.AppContextAware;
import org.openhds.domain.constraint.CheckIndividualGenderFemale;
import org.openhds.domain.model.Individual;
import org.openhds.domain.service.impl.SitePropertiesServiceImpl;

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

        if (individual.getGender().equals(properties.getFemaleCode()))
            return true;

        return false;
    }
}