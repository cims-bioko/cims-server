package com.github.cimsbioko.server.web.service.impl;

import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.EntityValidationService;
import com.github.cimsbioko.server.web.service.JsfService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class EntityValidationServiceJsfImpl<T> implements EntityValidationService<T> {

    JsfService jsfService;

    EntityValidationServiceJsfImpl(JsfService jsfService) {
        this.jsfService = jsfService;
    }

    public void validateEntity(T entityItem) throws ConstraintViolations {
        List<String> violations = validateType(entityItem);

        if (violations.size() > 0) {
            throw new ConstraintViolations(violations.get(0), violations);
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
