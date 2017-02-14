package com.github.cimsbioko.server.controller.service.refactor.impl;

import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.service.refactor.IndividualService;
import com.github.cimsbioko.server.controller.service.refactor.PregnancyObservationService;
import com.github.cimsbioko.server.controller.service.refactor.crudhelpers.EntityCrudHelper;
import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.model.PregnancyObservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PregnancyObservationServiceImpl implements PregnancyObservationService {

    @Autowired
    @Qualifier("PregnancyObservationCrudHelper")
    private EntityCrudHelper<PregnancyObservation> pregnancyObservationCrudHelper;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private IndividualService individualService;

    @Override
    public List<PregnancyObservation> getAll() {
        return pregnancyObservationCrudHelper.getAll();
    }

    @Override
    public PregnancyObservation getByExtId(String id) {
        return pregnancyObservationCrudHelper.getByExtId(id);
    }

    @Override
    public PregnancyObservation getByUuid(String id) {
        return pregnancyObservationCrudHelper.getByUuid(id);

    }

    @Override
    public void delete(PregnancyObservation pregnancyObservation) throws IllegalArgumentException {
        pregnancyObservationCrudHelper.delete(pregnancyObservation);
    }

    @Override
    public void create(PregnancyObservation pregnancyObservation) throws ConstraintViolations {
        pregnancyObservationCrudHelper.create(pregnancyObservation);
    }

    @Override
    public void save(PregnancyObservation pregnancyObservation) throws ConstraintViolations {
        pregnancyObservationCrudHelper.save(pregnancyObservation);
    }

    @Override
    public boolean isEligibleForCreation(PregnancyObservation pregnancyObservation, ConstraintViolations cv) {

        if (null == pregnancyObservation) {
            ConstraintViolations.addViolationIfNotNull(cv, "Null PregnancyObservation.");
            return false;
        }

        Individual mother = pregnancyObservation.getMother();

        boolean dead = individualService.isDeceased(mother);

        if (dead) {
            ConstraintViolations.addViolationIfNotNull(cv, "The referenced mother is deceased.");
        }


        return !dead;

    }

}
