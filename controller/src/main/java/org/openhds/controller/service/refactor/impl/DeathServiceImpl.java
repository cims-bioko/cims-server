package org.openhds.controller.service.refactor.impl;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.DeathService;
import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.controller.service.refactor.crudhelpers.EntityCrudHelper;
import org.openhds.domain.model.Death;
import org.openhds.domain.model.Individual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeathServiceImpl implements DeathService {

    @Autowired
    @Qualifier("DeathCrudHelper")
    private EntityCrudHelper<Death> deathCrudHelper;

    @Autowired
    IndividualService individualService;

    @Override
    public List<Death> getAll() {
        return deathCrudHelper.getAll();
    }

    @Override
    public Death getByExtId(String id) {
        return deathCrudHelper.getByExtId(id);
    }

    @Override
    public Death getByUuid(String id) {
        return deathCrudHelper.getByUuid(id);
    }

    @Override
    public boolean isEligibleForCreation(Death death, ConstraintViolations cv) {

        if (null == death) {
            ConstraintViolations.addViolationIfNotNull(cv, "Null Death.");
            return false;
        }

        Individual individual = death.getIndividual();

        boolean alreadyDead = individualService.isDeceased(individual);

        if (alreadyDead) {
            ConstraintViolations.addViolationIfNotNull(cv, "The referenced Individual is already deceased.");
        }

        return !alreadyDead;
    }

    @Override
    public void delete(Death death) throws IllegalArgumentException {
        deathCrudHelper.delete(death);
    }

    @Override
    public void create(Death death) throws ConstraintViolations {
        deathCrudHelper.create(death);
    }

    @Override
    public void save(Death death) throws ConstraintViolations {
        deathCrudHelper.save(death);
    }

    /*
            Extra methods

     */


}
