package com.github.cimsbioko.server.domain.constraint.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.github.cimsbioko.server.domain.service.impl.SitePropertiesServiceImpl;
import com.github.cimsbioko.server.domain.constraint.AppContextAware;
import com.github.cimsbioko.server.domain.constraint.CheckGenderOfRelationship;
import com.github.cimsbioko.server.domain.model.Relationship;

public class CheckGenderOfRelationshipImpl extends AppContextAware implements ConstraintValidator<CheckGenderOfRelationship, Relationship> {

    public void initialize(CheckGenderOfRelationship arg0) {
    }

    public boolean isValid(Relationship relationship, ConstraintValidatorContext arg1) {

        SitePropertiesServiceImpl properties = (SitePropertiesServiceImpl) context.getBean("siteProperties");

        if (relationship.getIndividualA().getGender().equals(properties.getMaleCode()) &&
                relationship.getIndividualB().getGender().equals(properties.getFemaleCode()))
            return true;
        return relationship.getIndividualA().getGender().equals(properties.getFemaleCode()) &&
                relationship.getIndividualB().getGender().equals(properties.getMaleCode());

    }
}
