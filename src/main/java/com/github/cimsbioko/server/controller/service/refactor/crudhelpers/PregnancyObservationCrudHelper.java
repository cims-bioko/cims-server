package com.github.cimsbioko.server.controller.service.refactor.crudhelpers;

import com.github.cimsbioko.server.controller.service.refactor.PregnancyObservationService;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.PregnancyObservation;
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
        return genericDao.findAll(PregnancyObservation.class, true);
    }

    @Override
    public PregnancyObservation getByExtId(String id) {
        return genericDao.findByProperty(PregnancyObservation.class, "extId", id, true);
    }

    @Override
    public PregnancyObservation getByUuid(String id) {
        return genericDao.findByProperty(PregnancyObservation.class, "uuid", id);
    }
}
