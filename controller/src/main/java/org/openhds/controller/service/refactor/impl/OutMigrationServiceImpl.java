package org.openhds.controller.service.refactor.impl;

import org.openhds.controller.exception.ConstraintViolations;


import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.controller.service.refactor.OutMigrationService;
import org.openhds.controller.service.refactor.ResidencyService;
import org.openhds.controller.service.refactor.crudhelpers.EntityCrudHelper;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.OutMigration;
import org.openhds.domain.model.Residency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public OutMigration read(String id) {

        return outMigrationCrudHelper.read(id);
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
