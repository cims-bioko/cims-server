package org.openhds.controller.service.refactor.crudhelpers;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.domain.model.Residency;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wolfe on 9/10/14.
 */

@Component("ResidencyCrudHelper")
public class ResidencyCrudHelper extends AbstractEntityCrudHelperImpl<Residency> {


    @Override
    protected void preCreateSanityChecks(Residency residency) throws ConstraintViolations {

    }

    @Override
    protected void cascadeReferences(Residency residency) throws ConstraintViolations {

    }

    @Override
    protected void validateReferences(Residency residency) throws ConstraintViolations {

    }

    @Override
    public List<Residency> getAll() {
        return genericDao.findAll(Residency.class, true);
    }

    @Override
    public Residency read(String id) {
        return genericDao.read(Residency.class, id);
    }
}
