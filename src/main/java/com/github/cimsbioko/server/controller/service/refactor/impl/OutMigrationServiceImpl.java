package com.github.cimsbioko.server.controller.service.refactor.impl;

import com.github.cimsbioko.server.controller.service.refactor.ResidencyService;
import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;


import com.github.cimsbioko.server.controller.service.refactor.IndividualService;
import com.github.cimsbioko.server.controller.service.refactor.OutMigrationService;
import com.github.cimsbioko.server.controller.service.refactor.crudhelpers.EntityCrudHelper;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.model.OutMigration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutMigrationServiceImpl implements OutMigrationService {


    @Autowired
    @Qualifier("OutMigrationCrudHelper")
    private EntityCrudHelper<OutMigration> outMigrationCrudHelper;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private IndividualService individualService;

    @Autowired
    private ResidencyService residencyService;

    @Override
    public List<OutMigration> getAll() {

        return outMigrationCrudHelper.getAll();
    }

    @Override
    public OutMigration getByExtId(String id) {

        return outMigrationCrudHelper.getByExtId(id);
    }

    @Override
    public OutMigration getByUuid(String id) {
        return outMigrationCrudHelper.getByUuid(id);
    }


    @Override
    public void delete(OutMigration outMigration) throws IllegalArgumentException {

        outMigrationCrudHelper.delete(outMigration);
    }

    @Override
    public void create(OutMigration outMigration) throws ConstraintViolations {

        outMigrationCrudHelper.create(outMigration);
    }

    @Override
    public void save(OutMigration outMigration) throws ConstraintViolations {

        outMigrationCrudHelper.save(outMigration);
    }

    @Override
    public boolean isEligibleForCreation(OutMigration outMigration, ConstraintViolations cv) {

        if (null == outMigration) {
            ConstraintViolations.addViolationIfNotNull(cv, "Null OutMigration.");
            return false;
        }

        Individual individual = outMigration.getIndividual();

        boolean dead = individualService.isDeceased(individual);
        boolean hasOpenResidency = residencyService.hasOpenResidency(individual);
        boolean MigrateBeforeResidencyStart = individual.getCurrentResidency().getStartDate().compareTo(outMigration.getRecordedDate()) > 0;

        if (dead) {
            ConstraintViolations.addViolationIfNotNull(cv, "The referenced Individual is deceased.");
        }

        if (!hasOpenResidency) {
            ConstraintViolations.addViolationIfNotNull(cv, "The referenced Individual's Residency is not open.");
        }

        if (MigrateBeforeResidencyStart) {
            ConstraintViolations.addViolationIfNotNull(cv, "The OutMigration's date takes place before the Residency's startDate.");
        }


        return !dead && hasOpenResidency && !MigrateBeforeResidencyStart;
    }



    /*
            Extra methods

     */


}
