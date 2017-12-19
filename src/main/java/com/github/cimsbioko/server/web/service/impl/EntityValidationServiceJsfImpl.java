package com.github.cimsbioko.server.web.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.cimsbioko.server.service.EntityValidationService;
import com.github.cimsbioko.server.web.service.JsfService;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

@SuppressWarnings("unchecked")
public class EntityValidationServiceJsfImpl<T> implements EntityValidationService<T> {

    JsfService jsfService;

    EntityValidationServiceJsfImpl(JsfService jsfService) {
        this.jsfService = jsfService;
    }

    public void validateEntity(T entityItem) throws ConstraintViolations {
        List<String> violations = validateType(entityItem);

        if (violations.size() > 0) {
            throw new ConstraintViolations(violations.get(0).toString(), violations);
        }
    }

    @SuppressWarnings("rawtypes")
    public <S> List validateType(S entity) {
        List<String> violations = new ArrayList<>();

        Validator validator = getValidator();
        Set<ConstraintViolation<S>> constraintViolations = validator.validate(entity);

        for (ConstraintViolation<S> constraint : constraintViolations) {
            violations.add(constraint.getMessage());
        }
        return violations;
    }

    private Validator getValidator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
