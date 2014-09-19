package org.openhds.controller.service.refactor.impl;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.SocialGroupService;
import org.openhds.controller.service.refactor.crudhelpers.EntityCrudHelper;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.SocialGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SocialGroupServiceImpl implements SocialGroupService {

    @Autowired
    @Qualifier("SocialGroupCrudHelper")
    private EntityCrudHelper<SocialGroup> socialGroupCrudHelper;

    @Autowired
    private GenericDao genericDao;

    @Override
    public List<SocialGroup> getAll() {
        return socialGroupCrudHelper.getAll();
    }

    @Override
    public SocialGroup read(String id) {
        return socialGroupCrudHelper.read(id);
    }

    @Override
    public void delete(SocialGroup socialGroup) throws IllegalArgumentException {
        socialGroupCrudHelper.delete(socialGroup);
    }

    @Override
    public void create(SocialGroup socialGroup) throws ConstraintViolations {
        socialGroupCrudHelper.create(socialGroup);
    }

    @Override
    public void save(SocialGroup socialGroup) throws ConstraintViolations {
        socialGroupCrudHelper.save(socialGroup);
    }

    @Override
    public boolean isEligibleForCreation(SocialGroup socialGroup, ConstraintViolations cv) {
        return false;
    }

}
