package org.openhds.controller.service.refactor.impl;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.MembershipService;
import org.openhds.controller.service.refactor.ResidencyService;
import org.openhds.controller.service.refactor.crudhelpers.EntityCrudHelper;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.Residency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResidencyServiceImpl implements ResidencyService {

    @Autowired
    @Qualifier("ResidencyCrudHelper")
    private EntityCrudHelper<Residency> residencyCrudHelper;

    @Autowired
    private GenericDao genericDao;


    @Override
    public List<Residency> getAll() {

        return residencyCrudHelper.getAll();
    }

    @Override
    public Residency read(String id) {

        return residencyCrudHelper.read(id);
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
        return false;
    }


    /*
            Extra methods

     */

    @Override
    public boolean hasOpenResidency(Individual individual) {

        Residency residency = individual.getCurrentResidency();

        if (null == residency || null != residency.getEndDate() || residency.isDeleted()) {
            return false;
        }

        return true;
    }


}
