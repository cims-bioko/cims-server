package org.openhds.controller.service.refactor.crudhelpers;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.InMigrationService;
import org.openhds.domain.model.InMigration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wolfe on 9/19/14.
 */

@Component("InMigrationCrudHelper")
public class InMigrationCrudHelper extends AbstractEntityCrudHelperImpl<InMigration> {


    @Autowired
    InMigrationService inMigrationService;

    @Override
    protected void preCreateSanityChecks(InMigration inMigration) throws ConstraintViolations {

    }

    @Override
    protected void cascadeReferences(InMigration inMigration) throws ConstraintViolations {

        String currentResidencyEndType = inMigration.getIndividual().getCurrentResidency().getEndType();
        if(null == currentResidencyEndType ||  sitePropertiesService.getNotApplicableCode().equals(currentResidencyEndType)){
            currentResidencyEndType = sitePropertiesService.getInmigrationCode();
        }

        inMigration.getIndividual().getAllResidencies().add(inMigration.getResidency());

    }

    @Override
    protected void validateReferences(InMigration inMigration) throws ConstraintViolations {

        ConstraintViolations constraintViolations = new ConstraintViolations();
        if (!inMigrationService.isEligibleForCreation(inMigration, constraintViolations)) {
            throw (constraintViolations);
        }

    }

    @Override
    public List<InMigration> getAll() {
        return genericDao.findAll(InMigration.class, true);
    }

    @Override
    public InMigration getByExtId(String id) {
        return genericDao.findByProperty(InMigration.class, "extId", id);
    }

    @Override
    public InMigration getByUuid(String id) {
        return genericDao.findByProperty(InMigration.class, "uuid", id);
    }
}
