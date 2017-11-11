package com.github.cimsbioko.server.domain.constraint;

import java.util.Calendar;
import javax.validation.ConstraintValidatorContext;

import com.github.cimsbioko.server.domain.model.Individual;

public abstract class CheckIndividualAge extends AppContextAware {

    protected int requiredAge;
    protected boolean allowNull;

    /**
     * Validate that an individual is older then some required age
     */
    public boolean isValid(Individual individual, ConstraintValidatorContext ctx) {

        if (allowNull && individual == null)
            return true;
        if (allowNull)
            return true;

        Calendar minAge = Calendar.getInstance();
        minAge.add(Calendar.YEAR, -(requiredAge));

        return individual.getDob().compareTo(minAge) < 0;

    }
}
