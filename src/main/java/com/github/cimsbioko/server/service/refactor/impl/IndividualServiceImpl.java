package com.github.cimsbioko.server.service.refactor.impl;

import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.refactor.IndividualService;
import com.github.cimsbioko.server.service.refactor.crudhelpers.EntityCrudHelper;
import com.github.cimsbioko.server.domain.Individual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndividualServiceImpl implements IndividualService {

    @Autowired
    @Qualifier("IndividualCrudHelper")
    private EntityCrudHelper<Individual> individualCrudHelper;

    @Autowired
    private GenericDao genericDao;

    @Override
    public int getExistingExtIdCount(String extId) {
        List<Individual> existingIndividuals = genericDao.findListByProperty(Individual.class, "extId", extId, true);
        return existingIndividuals == null ? 0 : existingIndividuals.size();
    }

    @Override
    public List<Individual> getAll() {
        return individualCrudHelper.getAll();
    }

    @Override
    public Individual getByExtId(String id) {
        return individualCrudHelper.getByExtId(id);
    }

    @Override
    public Individual getByUuid(String id) {
        return individualCrudHelper.getByUuid(id);
    }

    @Override
    public boolean isEligibleForCreation(Individual individual, ConstraintViolations cv) {

        if (null == individual) {
            ConstraintViolations.addViolationIfNotNull(cv, "Null individual.");
            return false;
        }

        boolean nullExtId = (null == individual.getExtId());

        if (nullExtId) {
            ConstraintViolations.addViolationIfNotNull(cv, "The individual has a null ExtId.");
        }

        return !nullExtId;
    }

    @Override
    public void delete(Individual individual) throws IllegalArgumentException {
        individualCrudHelper.delete(individual);
    }

    @Override
    public void create(Individual individual) throws ConstraintViolations {
        individualCrudHelper.create(individual);
    }

    @Override
    public void save(Individual individual) throws ConstraintViolations {
        individualCrudHelper.save(individual);
    }
}
