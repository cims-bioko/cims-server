package com.github.cimsbioko.server.domain.constraint.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.github.cimsbioko.server.domain.model.Relationship;
import com.github.cimsbioko.server.domain.constraint.CheckRelatedIndividuals;

public class CheckRelatedIndividualsImpl implements ConstraintValidator<CheckRelatedIndividuals, Relationship> {

    public void initialize(CheckRelatedIndividuals arg0) {
    }

    public boolean isValid(Relationship relationship, ConstraintValidatorContext arg1) {

        return !relationship.getIndividualA().getExtId().equals(relationship.getIndividualB().getExtId());
    }
}
