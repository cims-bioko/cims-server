package org.openhds.controller.service.refactor.crudhelpers;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.OutMigrationService;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.OutMigration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Created by wolfe on 9/10/14.
 */

@Component("OutMigrationCrudHelper")
public class OutMigrationCrudHelper extends AbstractEntityCrudHelperImpl<OutMigration> {


    @Autowired
    OutMigrationService outMigrationService;

    @Override
    protected void preCreateSanityChecks(OutMigration outMigration) throws ConstraintViolations {


        //TODO: are we doing this up here because the changes made in cascadeReferences are making this fail?
        //TODO: if that is the case, then the piece that is making it fail should be checked on its own up here and
        //TODO: the rest should be done in validateReferences() like all the other services.
        ConstraintViolations constraintViolations = new ConstraintViolations();
        if (!outMigrationService.isEligibleForCreation(outMigration, constraintViolations)) {
            throw (constraintViolations);
        }

    }

    @Override
    protected void cascadeReferences(OutMigration outMigration) throws ConstraintViolations {

        //change residency endtype and enddate
        //TODO: should the residency already by set by this point?
        outMigration.setResidency(outMigration.getIndividual().getCurrentResidency());
        outMigration.getResidency().setEndType(sitePropertiesService.getOutmigrationCode());
        outMigration.getResidency().setEndDate(outMigration.getRecordedDate());

        //change membership end dates and end types
        // TODO: is this valid? does moving away really make you an invalid member?
        Set<Membership> memberships = outMigration.getIndividual().getAllMemberships();
        if (!memberships.isEmpty()) {
            for (Membership membership : memberships) {

                //TODO: why is it possible that the endtype can be null if there is a not applicable code we can use?
                if (null == membership.getEndType() || membership.getEndType().equals(sitePropertiesService.getNotApplicableCode())) {
                    membership.setEndDate(outMigration.getRecordedDate());
                    membership.setEndType(sitePropertiesService.getOutmigrationCode());
                }
            }
        }


    }

    @Override
    protected void validateReferences(OutMigration outMigration) throws ConstraintViolations {
    }

    @Override
    public List<OutMigration> getAll() {
        return genericDao.findAll(OutMigration.class, true);
    }

    @Override
    public OutMigration getByExtId(String id) {
        return genericDao.findByProperty(OutMigration.class, "extId", id, true);
    }

    @Override
    public OutMigration getByUuid(String id) {
        return genericDao.findByProperty(OutMigration.class, "uuid", id);
    }

}
