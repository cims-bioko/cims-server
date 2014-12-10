package org.openhds.controller.service.refactor.impl;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.ResidencyService;
import org.openhds.controller.service.refactor.InMigrationService;
import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.controller.service.refactor.crudhelpers.EntityCrudHelper;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.InMigration;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Residency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InMigrationServiceImpl implements InMigrationService {

    @Autowired
    @Qualifier("InMigrationCrudHelper")
    private EntityCrudHelper<InMigration> inMigrationCrudHelper;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private IndividualService individualService;

    @Autowired
    private ResidencyService residencyService;


    @Override
    public List<InMigration> getAll() {
        return inMigrationCrudHelper.getAll();
    }

    @Override
    public InMigration getByExtId(String id) {
        return inMigrationCrudHelper.getByExtId(id);
    }

    @Override
    public InMigration getByUuid(String id) {
        return inMigrationCrudHelper.getByUuid(id);

    }

    @Override
    public boolean isEligibleForCreation(InMigration inMigration, ConstraintViolations cv) {

        Individual individual = inMigration.getIndividual();
        Residency residency = inMigration.getResidency();

        if(null == inMigration){
            ConstraintViolations.addViolationIfNotNull(cv, "Null inMigration.");
            return false;
        }
        if(null == individual){
            ConstraintViolations.addViolationIfNotNull(cv, "InMigration references null Individual.");
            return false;
        }
        if(null == residency){
            ConstraintViolations.addViolationIfNotNull(cv, "InMigration references null Residency.");
            return false;
        }


        //TODO: should these methods check to see if the required references are present? (i.e. an individual)
        boolean isDead = individualService.isDeceased(individual);

        if(isDead){
            ConstraintViolations.addViolationIfNotNull(cv, "InMigration references dead individual.");
        }


        return !isDead;
    }

    @Override
    public void delete(InMigration inMigration) throws IllegalArgumentException {
        inMigrationCrudHelper.delete(inMigration);
    }

    @Override
    public void create(InMigration inMigration) throws ConstraintViolations {
        inMigrationCrudHelper.create(inMigration);
    }

    @Override
    public void save(InMigration inMigration) throws ConstraintViolations {
        inMigrationCrudHelper.save(inMigration);
    }
}
