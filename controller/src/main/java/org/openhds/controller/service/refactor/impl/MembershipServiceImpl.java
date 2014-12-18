package org.openhds.controller.service.refactor.impl;

import org.openhds.controller.exception.ConstraintViolations;

import org.openhds.controller.service.refactor.MembershipService;
import org.openhds.controller.service.refactor.crudhelpers.EntityCrudHelper;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Membership;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MembershipServiceImpl implements MembershipService {

    @Autowired
    @Qualifier("MembershipCrudHelper")
    private EntityCrudHelper<Membership> membershipCrudHelper;

    @Autowired
    private GenericDao genericDao;

    @Override
    public List<Membership> getAll() {
        return membershipCrudHelper.getAll();
    }

    @Override
    public Membership getByExtId(String id) {
        return membershipCrudHelper.getByExtId(id);
    }

    @Override
    public Membership getByUuid(String id) {
        return membershipCrudHelper.getByUuid(id);
    }

    @Override
    public void delete(Membership membership) throws IllegalArgumentException {
        membershipCrudHelper.delete(membership);
    }

    @Override
    public void create(Membership membership) throws ConstraintViolations {
        membershipCrudHelper.create(membership);
    }

    @Override
    public void save(Membership membership) throws ConstraintViolations {
        membershipCrudHelper.save(membership);
    }

    @Override
    public boolean isEligibleForCreation(Membership membership, ConstraintViolations cv) {
        return false;
    }

}
