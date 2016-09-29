package org.openhds.controller.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.idgeneration.VisitGenerator;
import org.openhds.controller.service.EntityService;
import org.openhds.controller.service.VisitService;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.annotations.Authorized;
import org.openhds.domain.model.Round;
import org.openhds.domain.model.Visit;
import org.openhds.domain.service.SitePropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("visitServiceImpl")
public class VisitServiceImpl implements VisitService {

	@Autowired
    private GenericDao genericDao;
	@Autowired
    private VisitGenerator generator;
	@Autowired
    private EntityService entityService;
	@Autowired
    private SitePropertiesService siteProperties;

    public Visit evaluateVisit(Visit entityItem) throws ConstraintViolations {
        if (!checkValidRoundNumber(entityItem.getRoundNumber()))
            throw new ConstraintViolations("The Round Number specified is not a valid Round Number.");

        return entityItem;
    }

    public Visit generateId(Visit entityItem) throws ConstraintViolations {
        entityItem.setExtId(generator.generateId(entityItem));
        return entityItem;
    }

    public Visit checkVisit(Visit persistedItem, Visit entityItem) throws ConstraintViolations {

        if (!checkValidRoundNumber(entityItem.getRoundNumber()))
            throw new ConstraintViolations("The Round Number specified is not a valid Round Number.");
        return entityItem;
    }

    public void validateGeneralVisit(Visit visit) throws ConstraintViolations {
        if (!checkValidRoundNumber(visit.getRoundNumber()))
            throw new ConstraintViolations("The Round Number specified is not a valid Round Number.");
    }

    /**
     * Checks if the provided round number exists
     */
    public boolean checkValidRoundNumber(Integer roundNumber) {

        Round round = genericDao.findByProperty(Round.class, "roundNumber", roundNumber);
        return round != null;
    }

    /**
     * Retrieves all Visit extId's that contain the term provided. Used in performing autocomplete.
     */
    public List<String> getVisitExtIds(String term) {
        List<String> ids = new ArrayList<>();
        List<Visit> list = genericDao.findListByPropertyPrefix(Visit.class, "extId", term, 10, true);
        for (Visit visit : list) {
            ids.add(visit.getExtId());
        }

        return ids;
    }

    public Visit findVisitByExtId(String extId) {
        return genericDao.findByProperty(Visit.class, "extId", extId, true);
    }

    public Visit findVisitByUuid(String uuid) {
        Visit visit = genericDao.findByProperty(Visit.class, "uuid", uuid);
        return visit;
    }

    @Transactional
    public void createVisit(Visit visit) throws ConstraintViolations {
        assignId(visit);
        evaluateVisit(visit);
        visit.setStatus(siteProperties.getDataStatusValidCode());

        try {
            entityService.create(visit);
        } catch (IllegalArgumentException e) {
            // should never happen
        } catch (SQLException e) {
            throw new ConstraintViolations("There was a problem saving the visit to the database");
        }
    }

    private void assignId(Visit visit) throws ConstraintViolations {
        String id = visit.getExtId() == null ? "" : visit.getExtId();
        if (id.trim().isEmpty() && generator.isGenerated()) {
            generateId(visit); 
        }
    }

    @Override
    public List<Visit> getAllVisits() {
        List<Visit> visits = genericDao.findAll(Visit.class, true);
        return visits;
    }

    @Override
    @Authorized("VIEW_ENTITY")
    public List<Visit> getAllVisitsForRoundInRange(int round, Visit start, int pageSize) {
        Object startProp = start == null? null : start.getUuid();
        return genericDao.findPagedFilteredgt(Visit.class, "id", "roundNumber", round, startProp, pageSize);
    }

    @Override
    @Authorized("VIEW_ENTITY")
    public long getTotalVisitCountForRound(int roundNumber) {
        return genericDao.getTotalCountWithFilter(Visit.class, "roundNumber", roundNumber);
    }
}
