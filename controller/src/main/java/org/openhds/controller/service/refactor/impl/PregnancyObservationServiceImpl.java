package org.openhds.controller.service.refactor.impl;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.PregnancyObservationService;
import org.openhds.controller.service.refactor.crudhelpers.EntityCrudHelper;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.PregnancyObservation;
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

    @Override
    public List<PregnancyObservation> getAll() {
        return pregnancyObservationCrudHelper.getAll();
    }

    @Override
    public PregnancyObservation read(String id) {
        return pregnancyObservationCrudHelper.read(id);
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

        return true;

    }

}
