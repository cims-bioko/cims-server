package com.github.cimsbioko.server.controller.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.domain.model.Residency;
import com.github.cimsbioko.server.domain.util.CalendarUtil;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.service.CurrentUser;
import com.github.cimsbioko.server.controller.service.EntityValidationService;
import com.github.cimsbioko.server.controller.service.ResidencyService;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.model.Location;
import com.github.cimsbioko.server.domain.service.SitePropertiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the ResidencySerivce
 *
 * @author Dave
 */
public class ResidencyServiceImpl extends EntityServiceRefactoredImpl implements ResidencyService {

    private static final Logger log = LoggerFactory.getLogger(ResidencyServiceImpl.class);

    private GenericDao genericDao;

    public ResidencyServiceImpl(GenericDao genericDao, SitePropertiesService siteProperties, EntityValidationService entityValidationService,
                                CalendarUtil calendarUtil, CurrentUser currentUser) {
        super(genericDao, currentUser, calendarUtil, siteProperties, entityValidationService);
        this.genericDao = genericDao;
    }

    public Residency updateResidency(Residency residency) throws ConstraintViolations {
        evaluateResidencyBeforeUpdate(residency);
        save(residency);
        return residency;
    }

    public void evaluateResidencyBeforeUpdate(Residency residency) throws ConstraintViolations {
        //TODO: constraint checking before persisting a Residency update
        if (null == residency) {
            throw new ConstraintViolations("Cannot update a null Residency");
        }
    }

    public Residency evaluateResidency(Residency candidateResidency) throws ConstraintViolations {
        // Verify an Individual is associated with the Residency, otherwise the Residency would not make sense
        checkIndividualAssignedToResidency(candidateResidency);
        Individual indiv = candidateResidency.getIndividual();
        checkIndividualEligibleForNewResdency(indiv);
        Set<Residency> res = indiv.getAllResidencies();
        // integrity checks on previous residencies
        for (Residency previousResidency : res) {
            // its possible that the start residency being evaluated has already been persisted
            // this case happens when the user is editing a residency
            // no need to have these checks on the same residency
            if (previousResidency.getUuid().equals(candidateResidency.getUuid())
                    || previousResidency.isDeleted()) {
                continue;
            }
        }
        return candidateResidency;
    }

    private void checkIndividualEligibleForNewResdency(Individual indiv) throws ConstraintViolations {
        if (hasOpenResidency(indiv)) {
            throw new ConstraintViolations("The individual already has an open residency. You must close the current residency for this Individual before " +
                    "a new Residency can be created.");
        }
    }

    private void checkIndividualAssignedToResidency(Residency candidateResidency) throws ConstraintViolations {
        if (candidateResidency.getIndividual() == null) {
            log.debug("An Individual was not supplied for the Residency");
            throw new ConstraintViolations("An Individual must be supplied to check if the Residency is valid.");
        }
    }

    public List<Individual> getIndividualsByLocation(Location location) {
        // get a list of all residencies for a given location
        List<Residency> residencies = genericDao.findListByProperty(Residency.class, "location", location);
        Set<Individual> individuals = new TreeSet<>(new IndividualComparator());
        for (Residency residency : residencies) {
            if (!residency.getIndividual().isDeleted())
                individuals.add(residency.getIndividual());
        }
        // for each individual determine if this is there current residency
        Iterator<Individual> itr = individuals.iterator();
        while (itr.hasNext()) {
            Individual indiv = itr.next();
            if (!indiv.getCurrentResidency().getLocation().getUuid().equals(location.getUuid())) {
                itr.remove();
            }
        }
        return new ArrayList<>(individuals);
    }

    public boolean hasOpenResidency(Individual individual) {
        return individual.getCurrentResidency() != null && !individual.getCurrentResidency().isDeleted();
    }

    public Residency makeResidencyInstance(Individual individual, Location location, Calendar startDate, String startType, FieldWorker collectedBy) {
        Residency residency = new Residency();
        residency.setIndividual(individual);
        residency.setLocation(location);
        residency.setCollectedBy(collectedBy);
        return residency;
    }

    public List<Residency> getAllResidencies(Individual individual) {
        return genericDao.findListByProperty(Residency.class, "individual", individual, true);
    }

    @Override
    @Authorized("VIEW_ENTITY")
    public long getTotalResidencyCount() {
        return genericDao.getTotalCount(Residency.class);
    }

    @Override
    @Authorized("VIEW_ENTITY")
    public List<Residency> getAllResidenciesInRange(Residency start, int size) {
        Object startProp = start == null ? null : start.getUuid();
        return genericDao.findPaged(Residency.class, "id", startProp, size);
    }

    private class IndividualComparator implements Comparator<Individual> {

        public int compare(Individual indiv1, Individual indiv2) {
            return indiv1.getExtId().compareTo(indiv2.getExtId());
        }
    }
}