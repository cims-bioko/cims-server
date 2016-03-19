package org.openhds.controller.service.refactor.crudhelpers;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.domain.model.Membership;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wolfe on 9/10/14.
 */

@Component("MembershipCrudHelper")
public class MembershipCrudHelper extends AbstractEntityCrudHelperImpl<Membership> {


    @Override
    protected void preCreateSanityChecks(Membership membership) throws ConstraintViolations {

    }

    @Override
    protected void cascadeReferences(Membership membership) throws ConstraintViolations {

    }

    @Override
    protected void validateReferences(Membership membership) throws ConstraintViolations {

    }

    @Override
    public List<Membership> getAll() {
        return genericDao.findAll(Membership.class, true);
    }

    @Override
    public Membership getByExtId(String id) {
        return genericDao.findByProperty(Membership.class, "extId", id, true);
    }

    @Override
    public Membership getByUuid(String id) {
        return genericDao.findByProperty(Membership.class,"uuid",id);
    }
}
