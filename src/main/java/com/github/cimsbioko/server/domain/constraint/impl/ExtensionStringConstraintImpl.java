package com.github.cimsbioko.server.domain.constraint.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.github.cimsbioko.server.util.AppContextAware;
import com.github.cimsbioko.server.service.ValueConstraintService;
import com.github.cimsbioko.server.domain.constraint.ExtensionStringConstraint;

public class ExtensionStringConstraintImpl extends AppContextAware implements ConstraintValidator<ExtensionStringConstraint, String> {

    private String constraint;
    private boolean allowNull;
    private ValueConstraintService service;

    public void initialize(ExtensionStringConstraint annotation) {
        service = (ValueConstraintService) context.getBean("valueConstraintService");
        this.constraint = annotation.constraint();
        this.allowNull = annotation.allowNull();
    }

    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null)
            return true;

        if (allowNull && value.equals(""))
            return true;

        return service.isValidConstraintValue(constraint, value);
    }
}

