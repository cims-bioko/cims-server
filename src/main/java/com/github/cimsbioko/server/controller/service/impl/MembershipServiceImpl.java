package com.github.cimsbioko.server.controller.service.impl;

import java.util.List;

import com.github.cimsbioko.server.controller.service.CurrentUser;
import com.github.cimsbioko.server.controller.service.EntityValidationService;
import com.github.cimsbioko.server.controller.service.IndividualService;
import com.github.cimsbioko.server.controller.service.MembershipService;
import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.model.Membership;
import com.github.cimsbioko.server.domain.model.SocialGroup;
import com.github.cimsbioko.server.domain.service.SitePropertiesService;
import com.github.cimsbioko.server.domain.util.CalendarUtil;
import org.springframework.transaction.annotation.Transactional;

public class MembershipServiceImpl extends EntityServiceRefactoredImpl implements MembershipService {

    private IndividualService individualService;
    private GenericDao genericDao;

    public MembershipServiceImpl(GenericDao genericDao, IndividualService individualService,
                                 EntityValidationService entityValidationService, CalendarUtil calendarUtil) {
        super(genericDao, calendarUtil, entityValidationService);
        this.genericDao = genericDao;
        this.individualService = individualService;
    }

    public Membership evaluateMembershipBeforeCreate(Membership entityItem) throws ConstraintViolations {
        if (!checkDuplicateMembership(entityItem.getIndividual(), entityItem.getSocialGroup()))
            throw new ConstraintViolations(
                    "A Membership for the specified Social Group already exists.");
        if (individualService.getLatestEvent(entityItem.getIndividual()).equals("Death"))
            throw new ConstraintViolations(
                    "A Membership cannot be created for an Individual who has a Death event.");
        return entityItem;
    }

    public Membership evaluateMembershipBeforeUpdate(Membership membership) throws ConstraintViolations {

        //TODO: check constraints before persisting the Membership update
        if (null == membership) {
            throw new ConstraintViolations("Cannot update a null Membership");
        }
        return membership;

    }

    /**
     * Checks if a duplicate Membership already exists
     */
    public boolean checkDuplicateMembership(Individual indiv, SocialGroup group) {

        List<Membership> list = genericDao
                .findListByProperty(Membership.class, "individual", indiv);

        for (Membership item : list) {
            if (item.getSocialGroup().getExtId().equals(group.getExtId()) && !item.isDeleted())
                return false;
        }
        return true;
    }

    /**
     * Helper method for creating a membership. NOTE: This is only being used by
     * the pregnancy outcome web service method
     *
     * @param individual
     * @param sg
     * @param fw
     * @param relationToGroupHead
     * @return
     */
    public Membership createMembershipForPregnancyOutcome(Individual individual, SocialGroup sg, FieldWorker fw, String relationToGroupHead) {
        Membership membership = new Membership();
        membership.setIndividual(individual);
        membership.setSocialGroup(sg);
        membership.setCollectedBy(fw);
        membership.setbIsToA(relationToGroupHead);
        return membership;
    }

    public void validateGeneralMembership(Membership membership) throws ConstraintViolations {
        if (individualIsHeadOfSocialGroup(membership.getIndividual(), membership.getSocialGroup())) {
            throw new ConstraintViolations(
                    "A Membership cannot be created for an Individual who is the head of the Social Group");
        }
    }

    public List<Membership> getAllMemberships(Individual individual) {
        List<Membership> items = genericDao.findListByProperty(Membership.class, "individual",
                individual, true);
        return items;
    }

    public List<Membership> getAllMemberships() {
        List<Membership> items = genericDao.findAll(Membership.class, true);
        return items;
    }

    /**
     * Determine whether the Individual is the head of the Social Group.
     *
     * @param individual
     * @param socialGroup
     * @return true is the Individual is the head of the Social Group
     */
    private boolean individualIsHeadOfSocialGroup(Individual individual, SocialGroup socialGroup) {
        return socialGroup.getGroupHead().getExtId().equals(individual.getExtId());
    }

    public Membership updateMembership(Membership membership) throws ConstraintViolations {
        evaluateMembershipBeforeUpdate(membership);
        save(membership);
        return membership;

    }

    @Override
    @Transactional
    public void createMembership(Membership membership) throws ConstraintViolations {
        evaluateMembershipBeforeCreate(membership);
        create(membership);
    }

    @Override
    @Authorized("VIEW_ENTITY")
    public List<Membership> getAllMembershipsInRange(Membership start, int size) {
        Object startProp = start == null ? null : start.getUuid();
        return genericDao.findPaged(Membership.class, "id", startProp, size);
    }

    @Override
    @Authorized("VIEW_ENTITY")
    public long getTotalMembershipCount() {
        return genericDao.getTotalCount(Membership.class);
    }
}
