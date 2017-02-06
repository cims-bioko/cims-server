package com.github.cimsbioko.server.domain.constraint.impl;

import java.util.Calendar;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.github.cimsbioko.server.domain.constraint.CheckStartDateGreaterThanBirthDate;
import com.github.cimsbioko.server.domain.model.Membership;

public class CheckStartDateGreaterThanBirthDateImpl
        implements ConstraintValidator<CheckStartDateGreaterThanBirthDate, Membership> {

    public void initialize(CheckStartDateGreaterThanBirthDate checkStartDateGreaterThanBirthDate) {
    }

    public boolean isValid(Membership membership, ConstraintValidatorContext constraintValidatorContext) {

        Calendar startDate = membership.getStartDate();
        if (null == startDate) {
            return true;
        }

        Calendar birthDate = membership.getIndividual().getDob();
        if (null == birthDate) {
            return true;
        }

        return birthDate.before(startDate) || birthDate.equals(startDate);

    }
}