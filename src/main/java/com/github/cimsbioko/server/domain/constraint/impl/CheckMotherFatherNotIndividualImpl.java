package com.github.cimsbioko.server.domain.constraint.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.github.cimsbioko.server.domain.constraint.CheckMotherFatherNotIndividual;
import com.github.cimsbioko.server.domain.model.Individual;

public class CheckMotherFatherNotIndividualImpl implements ConstraintValidator<CheckMotherFatherNotIndividual, Individual> {

    public void initialize(CheckMotherFatherNotIndividual arg0) {
    }

    public boolean isValid(Individual individual, ConstraintValidatorContext arg1) {

        return !(individual.getMother().getExtId().equals(individual.getExtId()) ||
                individual.getFather().getExtId().equals(individual.getExtId()));
    }
}