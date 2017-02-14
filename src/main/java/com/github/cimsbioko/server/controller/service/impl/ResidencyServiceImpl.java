package com.github.cimsbioko.server.controller.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
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

    private GenericDao genericDao;
    private SitePropertiesService siteProperties;
    static Logger log = LoggerFactory.getLogger(ResidencyServiceImpl.class);

    public ResidencyServiceImpl(GenericDao genericDao, SitePropertiesService siteProperties, EntityValidationService entityValidationService,
                                CalendarUtil calendarUtil, CurrentUser currentUser) {
        super(genericDao, currentUser, calendarUtil, siteProperties, entityValidationService);
        this.genericDao = genericDao;
        this.siteProperties = siteProperties;
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

    /*
     * (non-Javadoc)
     * @see ResidencyService#evaluateStartingResidency(Residency)
     */
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

            checkStartDateConstraints(candidateResidency, previousResidency);
            checkEndDateConstraints(candidateResidency, previousResidency);
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

    private void checkStartDateConstraints(Residency candidateResidency, Residency previousResidency) throws ConstraintViolations {
        // 2 residencies cannot have the same start date - this would not make sense
        if (previousResidency.getStartDate().compareTo(candidateResidency.getStartDate()) == 0) {
            log.debug("A residency already exist with this start date for the Individual");
            throw new ConstraintViolations("Residency cannot have same start date as previous residency");
        }

        // determine if the start date of this residency is interleaved between any other previous
        // residencies for this individual
        if (previousResidency.getStartDate().before(candidateResidency.getStartDate()) && previousResidency.getEndDate().after(candidateResidency.getStartDate())) {
            log.debug("Residency start date is interleaved with Residency with uuid: " + previousResidency.getUuid());
            throw new ConstraintViolations("Residency cannot have a start date that is interleaved with a previous residency.");
        }
    }

    private void checkEndDateConstraints(Residency candidateResidency, Residency previousResidency) throws ConstraintViolations {
        if (candidateResidency.getEndDate() != null && previousResidency.getEndDate() != null) {
            // determine if end date of this residency is interleaved between any other previous
            // residencies for this individual
            if (candidateResidency.getEndDate().compareTo(previousResidency.getStartDate()) > 0 && candidateResidency.getEndDate().compareTo(previousResidency.getEndDate()) <= 0) {
                log.debug("Residency end date is interleaved with Residency with uuid: " + previousResidency.getUuid());
                throw new ConstraintViolations("Residency cannot have an end date that is interleaved with a previous residency.");
            }

            // determine if residency overlays a previous residency
            if (candidateResidency.getStartDate().compareTo(previousResidency.getStartDate()) <= 0 && candidateResidency.getEndDate().compareTo(previousResidency.getEndDate()) >= 0) {
                log.debug("Residency overlays residency with uuid: " + previousResidency.getUuid());
                throw new ConstraintViolations("Residency cannot overlay a previous residency.");
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see ResidencyService#getIndividualsByLocation(Location)
     */
    public List<Individual> getIndividualsByLocation(Location location) {
        // get a list of all residencies for a given location
        List<Residency> residencies = genericDao.findListByProperty(Residency.class, "location", location);

        /** Filter out residencies that have already ended */
        List<Residency> unendedResidencies = new ArrayList<>();
        for (Residency residency : residencies) {
            if (residency.getEndDate() == null) {
                unendedResidencies.add(residency);
            }
        }
        Set<Individual> individuals = new TreeSet<>(new IndividualComparator());

        for (Residency residency : unendedResidencies) {
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

    public List<Individual> getIndividualsByLocation(Location location, Date startDate, Date endDate) {
        // get a list of all residencies for a given location
        List<Residency> residencies = genericDao.findListByProperty(Residency.class, "location", location);

        /** Filter out residencies that have already ended */
        List<Residency> unendedResidencies = new ArrayList<>();
        for (Residency residency : residencies) {
            if (residency.getEndDate() == null) {
                unendedResidencies.add(residency);
            }
        }
        Set<Individual> individuals = new TreeSet<>(new IndividualComparator());

        for (Residency residency : unendedResidencies) {
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


    /*
     * (non-Javadoc)
     * @see ResidencyService#hasOpenResidency(Individual)
     */
    public boolean hasOpenResidency(Individual individual) {
        return !(individual.getCurrentResidency() == null || individual.getCurrentResidency().getEndDate() != null
                || individual.getCurrentResidency().isDeleted());

    }

    /*
     * (non-Javadoc)
     * @see ResidencyService#makeResidencyInstance(Individual, Location, java.util.Calendar, EventType)
     */
    public Residency makeResidencyInstance(Individual individual, Location location, Calendar startDate, String startType, FieldWorker collectedBy) {
        Residency residency = new Residency();

        residency.setIndividual(individual);
        residency.setLocation(location);
        residency.setStartDate(startDate);
        residency.setStartType(startType);
        residency.setCollectedBy(collectedBy);
        residency.setEndType(siteProperties.getNotApplicableCode());

        return residency;
    }

    public List<Residency> getAllResidencies(Individual individual) {
        List<Residency> list = genericDao.findListByProperty(Residency.class, "individual", individual, true);
        return list;
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
        List<Residency> list = genericDao.findPaged(Residency.class, "id", startProp, size);
        return list;
    }

    /**
     * Individual comparator to order individuals by extId ascending
     *
     * @author Dave
     */
    private class IndividualComparator implements Comparator<Individual> {

        public int compare(Individual indiv1, Individual indiv2) {
            return indiv1.getExtId().compareTo(indiv2.getExtId());
        }
    }
}