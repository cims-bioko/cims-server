package com.github.cimsbioko.server.controller.service.refactor.crudhelpers;

import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.service.refactor.SocialGroupService;
import com.github.cimsbioko.server.domain.model.SocialGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("SocialGroupCrudHelper")
public class SocialGroupCrudHelper extends AbstractEntityCrudHelperImpl<SocialGroup> {

    @Autowired
    SocialGroupService socialGroupService;

    @Override
    protected void preCreateSanityChecks(SocialGroup socialGroup) throws ConstraintViolations {
    }

    @Override
    protected void cascadeReferences(SocialGroup socialGroup) throws ConstraintViolations {
    }

    @Override
    protected void validateReferences(SocialGroup socialGroup) throws ConstraintViolations {
        ConstraintViolations constraintViolations = new ConstraintViolations();
        if (!socialGroupService.isEligibleForCreation(socialGroup, constraintViolations)) {
            throw constraintViolations;
        }
    }

    @Override
    public List<SocialGroup> getAll() {
        return genericDao.findAll(SocialGroup.class, true);
    }

    @Override
    public SocialGroup getByExtId(String id) {
        return genericDao.findByProperty(SocialGroup.class, "extId", id, true);
    }

    @Override
    public SocialGroup getByUuid(String id) {
        return genericDao.findByProperty(SocialGroup.class, "uuid", id);
    }
}
