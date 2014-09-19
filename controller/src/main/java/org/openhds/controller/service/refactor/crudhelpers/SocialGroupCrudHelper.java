package org.openhds.controller.service.refactor.crudhelpers;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.SocialGroup;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wolfe on 9/10/14.
 */

@Component("SocialGroupCrudHelper")
public class SocialGroupCrudHelper extends AbstractEntityCrudHelperImpl<SocialGroup> {


    @Override
    protected void preCreateSanityChecks(SocialGroup socialGroup) throws ConstraintViolations {

    }

    @Override
    protected void cascadeReferences(SocialGroup socialGroup) throws ConstraintViolations {

    }

    @Override
    protected void validateReferences(SocialGroup socialGroup) {

    }

    @Override
    public List<SocialGroup> getAll() {
        return genericDao.findAll(SocialGroup.class, true);
    }

    @Override
    public SocialGroup read(String id) {
        return genericDao.read(SocialGroup.class, id);
    }
}
