package com.github.cimsbioko.server.service.refactor.crudhelpers;

import com.github.cimsbioko.server.domain.Individual;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.idgen.IndividualGenerator;
import com.github.cimsbioko.server.service.refactor.IndividualService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("IndividualCrudHelper")
public class IndividualCrudHelper extends AbstractEntityCrudHelperImpl<Individual> {

    @Autowired
    IndividualService individualService;

    @Autowired
    IndividualGenerator individualGenerator;

    @Override
    protected void preCreateSanityChecks(Individual individual) {
    }

    @Override
    protected void cascadeReferences(Individual individual) throws ConstraintViolations {
        if (null == individual.getExtId()) {
            individualGenerator.generateId(individual);
        }
    }


    @Override
    protected void validateReferences(Individual individual) throws ConstraintViolations {
        ConstraintViolations constraintViolations = new ConstraintViolations();
        if (!individualService.isEligibleForCreation(individual, constraintViolations)) {
            throw (constraintViolations);
        }
    }

    @Override
    public List<Individual> getAll() {
        return genericDao.findAll(Individual.class, true);
    }

    @Override
    public Individual getByExtId(String id) {
        return genericDao.findByProperty(Individual.class, "extId", id, true);
    }

    @Override
    public Individual getByUuid(String id) {
        return genericDao.findByProperty(Individual.class, "uuid", id);
    }
}
