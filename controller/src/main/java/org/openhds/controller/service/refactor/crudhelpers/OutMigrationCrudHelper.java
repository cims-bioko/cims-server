package org.openhds.controller.service.refactor.crudhelpers;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.MembershipService;
import org.openhds.controller.service.refactor.ResidencyService;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.OutMigration;
import org.openhds.domain.model.Residency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Created by wolfe on 9/10/14.
 */

@Component("OutMigrationCrudHelper")
public class OutMigrationCrudHelper extends AbstractEntityCrudHelperImpl<OutMigration> {


    @Override
    protected void preCreateSanityChecks(OutMigration outMigration) throws ConstraintViolations {

    }

    @Override
    protected void cascadeReferences(OutMigration outMigration) throws ConstraintViolations {

    }

    @Override
    protected void validateReferences(OutMigration outMigration) {
    }

    @Override
    public List<OutMigration> getAll() {
        return genericDao.findAll(OutMigration.class, true);
    }

    @Override
    public OutMigration read(String id) {
        return genericDao.read(OutMigration.class, id);
    }

}
