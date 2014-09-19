package org.openhds.controller.service.refactor.crudhelpers;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.DeathService;
import org.openhds.domain.model.Death;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.Relationship;
import org.openhds.domain.model.Residency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * Created by wolfe on 9/15/14.
 */

@Component("DeathCrudHelper")
public class DeathCrudHelper extends AbstractEntityCrudHelperImpl<Death> {

    //TODO: do we really want to calculate the age at death this way?
    private static final int MILLISECONDS_IN_DAY = 86400000;

    @Autowired
    DeathService deathService;

    @Override
    protected void preCreateSanityChecks(Death death) throws ConstraintViolations {

        ConstraintViolations constraintViolations = new ConstraintViolations();
        if (!deathService.isEligibleForCreation(death, constraintViolations)) {
            throw (constraintViolations);
        }

    }

    @Override
    protected void cascadeReferences(Death death) throws ConstraintViolations {

        Calendar endDate = death.getDeathDate();
        Residency residency;
        if (null != (residency = death.getIndividual().getCurrentResidency())) {
            residency.setEndDate(endDate);
            residency.setEndType(sitePropertiesService.getDeathCode());
        }

        Long ageAtDeath = (endDate.getTimeInMillis() - death.getIndividual().getDob().getTimeInMillis())/MILLISECONDS_IN_DAY;
        death.setAgeAtDeath(ageAtDeath);

        //Gets the individual's memberships if any
        // Iterates through memberships and sets endType(DEATH) and endDate
        if (!death.getIndividual().getAllMemberships().isEmpty()) {
            Set<Membership> memberships = death.getIndividual().getAllMemberships();
            for (Membership membership : memberships) {
                if (null == membership.getEndType() || membership.getEndType().equals(sitePropertiesService.getNotApplicableCode())) {
                    membership.setEndDate(endDate);
                    membership.setEndType(sitePropertiesService.getDeathCode());
                }
            }
        }

        //Gets the individual's relationships if any
        // Iterates through the relationships and sets endType(DEATH) and endDate
        if (!death.getIndividual().getAllRelationships1().isEmpty()) {
            Set<Relationship> relationships = death.getIndividual().getAllRelationships1();
            for (Relationship relationship : relationships) {
                if (null == relationship.getEndType() || relationship.getEndType().equals(sitePropertiesService.getNotApplicableCode())) {
                    relationship.setEndDate(endDate);
                    relationship.setEndType(sitePropertiesService.getDeathCode());
                }
            }
        }

        if (!death.getIndividual().getAllRelationships1().isEmpty()) {
            Set<Relationship> relationships = death.getIndividual().getAllRelationships2();
            for (Relationship relationship : relationships) {
                if (null == relationship.getEndType() || relationship.getEndType().equals(sitePropertiesService.getNotApplicableCode())) {
                    relationship.setEndDate(endDate);
                    relationship.setEndType(sitePropertiesService.getDeathCode());
                }
            }
        }

    }

    @Override
    protected void validateReferences(Death death) throws ConstraintViolations {

    }

    @Override
    public List<Death> getAll() {
        return genericDao.findAll(Death.class, true);
    }

    @Override
    public Death read(String id) {
        return genericDao.read(Death.class, id);
    }
}
