package org.openhds.controller.service.refactor.impl;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.controller.service.refactor.MembershipService;
import org.openhds.controller.service.refactor.crudhelpers.EntityCrudHelper;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Death;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Membership;
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
    public List<Individual> getAll() {
        return individualCrudHelper.getAll();
    }

    @Override
    public Individual read(String id) {
        return individualCrudHelper.read(id);
    }

    @Override
    public boolean isEligibleForCreation(Individual individual, ConstraintViolations cv) {
        return false;
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


    /*
            Extra methods

     */



    @Override
    public boolean isDeceased(Individual individual) {
        //TODO: refactor the "getLatestEvent" logic in the old IndividualService
        return (null != genericDao.findByProperty(Death.class, "individual", individual, true));

    }
}
