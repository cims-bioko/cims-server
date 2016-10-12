package org.openhds.controller.service.refactor.impl;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.controller.service.refactor.SocialGroupService;
import org.openhds.controller.service.refactor.crudhelpers.EntityCrudHelper;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.annotations.Authorized;
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
    private IndividualService individualService;

    @Autowired
    private GenericDao genericDao;

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

        boolean isDead = individualService.isDeceased(socialGroup.getGroupHead());
        if (isDead) {
            ConstraintViolations.addViolationIfNotNull(cv, "Head is dead.");
        }

        boolean hasExtId = socialGroup.getExtId() != null && !socialGroup.getExtId().isEmpty();
        if (!hasExtId) {
            ConstraintViolations.addViolationIfNotNull(cv, "Null or empty extId.");
        }

        return !isDead && hasExtId;

    }

    @Override
    @Authorized("VIEW_ENTITY")
    public List<SocialGroup> getAllSocialGroupsInRange(int i, int pageSize) {
        return genericDao.findPaged(SocialGroup.class, "extId", i, pageSize);
    }

    @Override
    @Authorized("VIEW_ENTITY")
    public long getTotalSocialGroupCount() {
        return genericDao.getTotalCount(SocialGroup.class);
    }

}
