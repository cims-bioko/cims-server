package org.openhds.controller.service.refactor.impl;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.InMigrationService;
import org.openhds.controller.service.refactor.MembershipService;
import org.openhds.controller.service.refactor.crudhelpers.EntityCrudHelper;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.InMigration;
import org.openhds.domain.model.Membership;
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


    @Override
    public List<InMigration> getAll() {
        return inMigrationCrudHelper.getAll();
    }

    @Override
    public InMigration read(String id) {
        return inMigrationCrudHelper.read(id);
    }

    @Override
    public boolean isEligibleForCreation(InMigration entity, ConstraintViolations cv) {

        return true;
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
