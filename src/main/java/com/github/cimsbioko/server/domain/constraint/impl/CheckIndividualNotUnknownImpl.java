package com.github.cimsbioko.server.domain.constraint.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.github.cimsbioko.server.domain.constraint.CheckIndividualNotUnknown;
import com.github.cimsbioko.server.domain.service.impl.SitePropertiesServiceImpl;
import com.github.cimsbioko.server.domain.constraint.AppContextAware;
import com.github.cimsbioko.server.domain.model.Individual;

public class CheckIndividualNotUnknownImpl extends AppContextAware implements ConstraintValidator<CheckIndividualNotUnknown, Individual> {

    private SitePropertiesServiceImpl properties;

    public void initialize(CheckIndividualNotUnknown constraintAnnotation) {
        properties = (SitePropertiesServiceImpl) context.getBean("siteProperties");
    }

    public boolean isValid(Individual value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return !value.getExtId().equals(properties.getUnknownIdentifier());

    }
}
