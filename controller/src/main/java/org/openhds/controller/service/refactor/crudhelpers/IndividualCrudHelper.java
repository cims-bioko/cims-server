package org.openhds.controller.service.refactor.crudhelpers;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.domain.model.Individual;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wolfe on 9/10/14.
 */

@Component("IndividualCrudHelper")
public class IndividualCrudHelper extends AbstractEntityCrudHelperImpl<Individual> {


    @Override
    protected void preCreateSanityChecks(Individual individual) throws ConstraintViolations {

    }

    @Override
    protected void cascadeReferences(Individual individual) throws ConstraintViolations {

    }

    @Override
    protected void validateReferences(Individual individual) throws ConstraintViolations {

    }

    @Override
    public List<Individual> getAll() {
        return genericDao.findAll(Individual.class,true);
    }

    @Override
    public Individual read(String id) {
        return genericDao.findByProperty(Individual.class,"extId",id);
    }
}
