package com.github.cimsbioko.server.service;

import java.util.List;

import com.github.cimsbioko.server.exception.ConstraintViolations;

public interface EntityValidationService<T> {

    <S> List<String> validateType(S entity);

    void validateEntity(T entityItem) throws ConstraintViolations;
}
