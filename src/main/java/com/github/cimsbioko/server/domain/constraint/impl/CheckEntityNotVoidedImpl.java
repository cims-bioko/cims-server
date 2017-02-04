package com.github.cimsbioko.server.domain.constraint.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.github.cimsbioko.server.domain.constraint.CheckEntityNotVoided;
import com.github.cimsbioko.server.domain.model.AuditableEntity;

public class CheckEntityNotVoidedImpl implements ConstraintValidator<CheckEntityNotVoided, AuditableEntity> {

    private boolean allowNull;

    public void initialize(CheckEntityNotVoided arg0) {
        this.allowNull = arg0.allowNull();

    }

    public boolean isValid(AuditableEntity auditableEntity, ConstraintValidatorContext context) {

        if (allowNull && auditableEntity == null) {
            return true;
        }

        return !auditableEntity.isDeleted();
    }
}
