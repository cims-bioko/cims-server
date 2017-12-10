package com.github.cimsbioko.server.controller.service.refactor.impl;

import com.github.cimsbioko.server.controller.service.refactor.IndividualService;
import com.github.cimsbioko.server.controller.service.refactor.crudhelpers.EntityCrudHelper;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.service.refactor.SocialGroupService;
import com.github.cimsbioko.server.domain.model.SocialGroup;
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
    private IndividualService individualService;

    @Override
    public List<SocialGroup> getAll() {
        return socialGroupCrudHelper.getAll();
    }

    @Override
    public SocialGroup getByExtId(String id) {
        return socialGroupCrudHelper.getByExtId(id);
    }

    @Override
    public SocialGroup getByUuid(String id) {
        return socialGroupCrudHelper.getByUuid(id);
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

        boolean isDead = individualService.isDeceased(socialGroup.getHead());
        if (isDead) {
            ConstraintViolations.addViolationIfNotNull(cv, "Head is dead.");
        }

        boolean hasExtId = socialGroup.getExtId() != null && !socialGroup.getExtId().isEmpty();
        if (!hasExtId) {
            ConstraintViolations.addViolationIfNotNull(cv, "Null or empty extId.");
        }

        return !isDead && hasExtId;

    }
}
