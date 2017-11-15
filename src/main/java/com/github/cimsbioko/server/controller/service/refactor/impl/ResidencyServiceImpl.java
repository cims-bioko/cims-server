package com.github.cimsbioko.server.controller.service.refactor.impl;

import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.service.refactor.EntityService;
import com.github.cimsbioko.server.controller.service.refactor.crudhelpers.EntityCrudHelper;
import com.github.cimsbioko.server.domain.model.Residency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResidencyServiceImpl implements EntityService<Residency> {

    @Autowired
    @Qualifier("ResidencyCrudHelper")
    private EntityCrudHelper<Residency> residencyCrudHelper;

    @Override
    public List<Residency> getAll() {
        return residencyCrudHelper.getAll();
    }

    @Override
    public Residency getByExtId(String id) {
        return residencyCrudHelper.getByExtId(id);
    }

    @Override
    public Residency getByUuid(String id) {
        return residencyCrudHelper.getByUuid(id);
    }

    @Override
    public void delete(Residency residency) throws IllegalArgumentException {
        residencyCrudHelper.delete(residency);
    }

    @Override
    public void create(Residency residency) throws ConstraintViolations {
        residencyCrudHelper.create(residency);
    }

    @Override
    public void save(Residency residency) throws ConstraintViolations {
        residencyCrudHelper.save(residency);
    }

    @Override
    public boolean isEligibleForCreation(Residency residency, ConstraintViolations cv) {
        return true;
    }
}
