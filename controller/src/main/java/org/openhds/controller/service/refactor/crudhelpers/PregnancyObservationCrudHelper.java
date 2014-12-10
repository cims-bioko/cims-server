package org.openhds.controller.service.refactor.crudhelpers;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.PregnancyObservationService;
import org.openhds.domain.model.PregnancyObservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wolfe on 9/15/1913.
 */

@Component("PregnancyObservationCrudHelper")
public class PregnancyObservationCrudHelper extends AbstractEntityCrudHelperImpl<PregnancyObservation> {

    @Autowired
    private PregnancyObservationService pregnancyObservationService;


    @Override
    protected void preCreateSanityChecks(PregnancyObservation pregnancyObservation) throws ConstraintViolations {



    }

    @Override
    protected void cascadeReferences(PregnancyObservation pregnancyObservation) throws ConstraintViolations {

    }

    @Override
    protected void validateReferences(PregnancyObservation pregnancyObservation) throws ConstraintViolations {

        ConstraintViolations constraintViolations = new ConstraintViolations();
        if (!pregnancyObservationService.isEligibleForCreation(pregnancyObservation, constraintViolations)) {
            throw (constraintViolations);
        }

    }

    @Override
    public List<PregnancyObservation> getAll() {
        return genericDao.findAll(PregnancyObservation.class,true);
    }

    @Override
    public PregnancyObservation getByExtId(String id) {
        return genericDao.findByProperty(PregnancyObservation.class,"extId",id);
    }

    @Override
    public PregnancyObservation getByUuid(String id) {
        return genericDao.findByProperty(PregnancyObservation.class,"uuid",id);
    }
}
