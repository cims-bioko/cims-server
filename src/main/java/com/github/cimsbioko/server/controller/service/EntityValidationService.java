package com.github.cimsbioko.server.controller.service;

import java.util.List;

import com.github.cimsbioko.server.controller.exception.ConstraintViolations;

public interface EntityValidationService<T> {

    boolean checkConstraints(T entityItem);

    <S> List<String> validateType(S entity);

    void validateEntity(T entityItem) throws ConstraintViolations;

    void setStatusPending(T entityItem);

    void setStatusVoided(T entityItem);
}
