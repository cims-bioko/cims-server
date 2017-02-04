package com.github.cimsbioko.server.domain.constraint.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.github.cimsbioko.server.domain.constraint.CheckDropdownMenuItemSelected;
import com.github.cimsbioko.server.domain.model.InMigration;

public class CheckDropdownMenuItemSelectedImpl implements ConstraintValidator<CheckDropdownMenuItemSelected, InMigration> {

    public void initialize(CheckDropdownMenuItemSelected arg0) {
    }

    public boolean isValid(InMigration arg0, ConstraintValidatorContext arg1) {

        return !arg0.getReason().equals("BLANK");

    }


}
