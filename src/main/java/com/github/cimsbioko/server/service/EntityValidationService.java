package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.exception.ConstraintViolations;

import java.util.List;

public interface EntityValidationService<T> {

    <S> List<String> validateType(S entity);

    void validateEntity(T entityItem) throws ConstraintViolations;
}
