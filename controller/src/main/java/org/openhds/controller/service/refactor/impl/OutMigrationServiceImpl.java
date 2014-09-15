package org.openhds.controller.service.refactor.impl;

import org.openhds.controller.exception.ConstraintViolations;


import org.openhds.controller.service.refactor.OutMigrationService;
import org.openhds.controller.service.refactor.crudhelpers.EntityCrudHelper;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.OutMigration;
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

}
