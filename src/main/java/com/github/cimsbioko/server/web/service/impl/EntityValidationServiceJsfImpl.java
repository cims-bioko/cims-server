package com.github.cimsbioko.server.web.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.cimsbioko.server.controller.service.EntityValidationService;
import com.github.cimsbioko.server.domain.service.SitePropertiesService;
import com.github.cimsbioko.server.web.service.JsfService;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

@SuppressWarnings("unchecked")
public class EntityValidationServiceJsfImpl<T> implements EntityValidationService<T> {

    private static final Logger log = LoggerFactory.getLogger(EntityValidationServiceJsfImpl.class);

    SitePropertiesService siteProperties;
    JsfService jsfService;

    EntityValidationServiceJsfImpl(SitePropertiesService siteProperties, JsfService jsfService) {
        this.siteProperties = siteProperties;
        this.jsfService = jsfService;
    }

    public void validateEntity(T entityItem) throws ConstraintViolations {
        List<String> violations = validateType(entityItem);

        if (violations.size() > 0) {
            throw new ConstraintViolations(violations.get(0).toString(), violations);
        }
    }

    public boolean checkConstraints(T entityItem) {
        Validator validator = getValidator();
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(entityItem);

        if (constraintViolations.size() > 0) {
            for (ConstraintViolation<T> constraintViolation : constraintViolations) {
                jsfService.addError(constraintViolation.getMessage());
            }
            return true;
        }
        return false;
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
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        return validator;
    }
}
