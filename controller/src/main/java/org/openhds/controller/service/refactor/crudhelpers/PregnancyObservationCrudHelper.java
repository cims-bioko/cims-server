package org.openhds.controller.service.refactor.crudhelpers;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.domain.model.PregnancyObservation;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wolfe on 9/15/14.
 */

@Component("PregnancyObservationCrudHelper")
public class PregnancyObservationCrudHelper extends AbstractEntityCrudHelperImpl<PregnancyObservation> {


    @Override
    protected void preCreateSanityChecks(PregnancyObservation individual) throws ConstraintViolations {

    }

    @Override
    protected void cascadeReferences(PregnancyObservation individual) throws ConstraintViolations {

    }

    @Override
    protected void validateReferences(PregnancyObservation individual) {

    }

    @Override
    public List<PregnancyObservation> getAll() {
        return genericDao.findAll(PregnancyObservation.class,true);
    }

    @Override
    public PregnancyObservation read(String id) {
        return genericDao.findByProperty(PregnancyObservation.class,"extId",id);
    }
}
