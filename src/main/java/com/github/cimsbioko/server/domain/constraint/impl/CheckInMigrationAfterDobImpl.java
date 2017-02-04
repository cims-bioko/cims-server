package com.github.cimsbioko.server.domain.constraint.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.github.cimsbioko.server.domain.model.InMigration;
import com.github.cimsbioko.server.domain.constraint.CheckInMigrationAfterDob;
import com.github.cimsbioko.server.domain.model.Individual;

public class CheckInMigrationAfterDobImpl implements ConstraintValidator<CheckInMigrationAfterDob, InMigration> {

    public void initialize(CheckInMigrationAfterDob constraintAnnotation) {
    }

    /**
     * The in migration date should be after the individuals dob
     */
    public boolean isValid(InMigration value, ConstraintValidatorContext context) {
        Individual indiv = value.getIndividual();

        return value.getRecordedDate().compareTo(indiv.getDob()) > 0;

    }
}
