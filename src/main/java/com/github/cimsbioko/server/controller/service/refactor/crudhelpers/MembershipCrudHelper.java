package com.github.cimsbioko.server.controller.service.refactor.crudhelpers;

import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Membership;
import org.springframework.stereotype.Component;

import java.util.List;

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
        return genericDao.findByProperty(Membership.class, "uuid", id);
    }
}
