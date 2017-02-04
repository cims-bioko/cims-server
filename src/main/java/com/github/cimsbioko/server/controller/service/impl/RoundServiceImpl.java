package com.github.cimsbioko.server.controller.service.impl;

import java.util.List;

import com.github.cimsbioko.server.controller.service.EntityService;
import com.github.cimsbioko.server.dao.service.GenericDao;
import com.github.cimsbioko.server.domain.model.Round;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.service.RoundService;

public class RoundServiceImpl implements RoundService {

    private EntityService entityService;
    private GenericDao genericDao;

    public RoundServiceImpl(GenericDao genericDao, EntityService entityService) {
        this.genericDao = genericDao;
        this.entityService = entityService;
    }

    public void evaluateRound(Round round) throws ConstraintViolations {
        if (round.getUuid() == null && isDuplicateRoundNumber(round)) {
            throw new ConstraintViolations(
                    "A round already exists with that round number. Please enter in a unique round number.");
        }
    }

    private boolean isDuplicateRoundNumber(Round round) {
        Round r = genericDao.findByProperty(Round.class, "roundNumber", round.getRoundNumber());
        return r != null;
    }

    @Override
    public List<Round> getAllRounds() {
        return genericDao.findAll(Round.class, false);
    }
}
