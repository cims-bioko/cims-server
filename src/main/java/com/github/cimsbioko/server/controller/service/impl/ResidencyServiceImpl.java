package com.github.cimsbioko.server.controller.service.impl;

import java.util.List;

import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.domain.model.Residency;
import com.github.cimsbioko.server.domain.util.CalendarUtil;
import com.github.cimsbioko.server.controller.service.EntityValidationService;
import com.github.cimsbioko.server.controller.service.ResidencyService;
import com.github.cimsbioko.server.domain.model.Individual;

public class ResidencyServiceImpl extends EntityServiceRefactoredImpl implements ResidencyService {

    private GenericDao genericDao;

    public ResidencyServiceImpl(GenericDao genericDao, EntityValidationService entityValidationService,
                                CalendarUtil calendarUtil) {
        super(genericDao, calendarUtil, entityValidationService);
        this.genericDao = genericDao;
    }

    public boolean hasOpenResidency(Individual individual) {
        return individual.getCurrentResidency() != null && !individual.getCurrentResidency().isDeleted();
    }

    public List<Residency> getAllResidencies(Individual individual) {
        return genericDao.findListByProperty(Residency.class, "individual", individual, true);
    }
}