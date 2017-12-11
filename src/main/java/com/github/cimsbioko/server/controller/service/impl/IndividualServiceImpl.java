package com.github.cimsbioko.server.controller.service.impl;

import java.sql.SQLException;

import com.github.cimsbioko.server.controller.service.EntityService;
import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.idgeneration.IndividualGenerator;
import com.github.cimsbioko.server.controller.service.IndividualService;
import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.service.SitePropertiesService;
import org.springframework.transaction.annotation.Transactional;

public class IndividualServiceImpl implements IndividualService {

    private GenericDao genericDao;
    private SitePropertiesService properties;
    private IndividualGenerator indivGen;
    private EntityService entityService;

    public IndividualServiceImpl(GenericDao genericDao, IndividualGenerator generator,
                                 SitePropertiesService properties, EntityService entityService) {
        this.genericDao = genericDao;
        this.indivGen = generator;
        this.properties = properties;
        this.entityService = entityService;
    }

    public Individual evaluateIndividual(Individual entityItem) throws ConstraintViolations {
        if (entityItem.getExtId() == null)
            assignId(entityItem);
        if (findIndivById(entityItem.getExtId()) != null)
            throw new ConstraintViolations("The Id specified already exists");

        validateIdLength(entityItem);

        return entityItem;
    }

    @Transactional
    public void createIndividual(Individual individual) throws ConstraintViolations {
        assignId(individual);
        evaluateIndividual(individual);
        try {
            entityService.create(individual);
        } catch (IllegalArgumentException e) {
            throw new ConstraintViolations("IllegalArgumentException creating individual: " + e);
        } catch (SQLException e) {
            throw new ConstraintViolations("SQLException creating individual: " + e);
        }
    }

    private void assignId(Individual individual) throws ConstraintViolations {
        String id = individual.getExtId() == null ? "" : individual.getExtId();
        if (id.trim().isEmpty() && indivGen.generated) {
            generateId(individual);
        }
    }

    public Individual validateIdLength(Individual entityItem) throws ConstraintViolations {
        indivGen.validateIdLength(entityItem.getExtId(), indivGen.getIdScheme());
        return entityItem;
    }

    public Individual generateId(Individual entityItem) throws ConstraintViolations {
        entityItem.setExtId(indivGen.generateId(entityItem));
        return entityItem;
    }

    @Transactional(readOnly = true)
    public Individual findIndivById(String indivExtId) {
        Individual indiv = genericDao.findByProperty(Individual.class, "extId", indivExtId, true);
        return indiv;
    }

    @Transactional(rollbackFor = Exception.class)
    public Individual createTemporaryIndividualWithExtId(String extId, FieldWorker collectedBy)
            throws Exception {
        Individual head = new Individual();
        head.setFirstName("Temporary Individual");
        head.setLastName("Temporary Individual");
        head.setExtId(extId);
        head.setGender(properties.getUnknownIdentifier());
        head.setCollector(collectedBy);
        entityService.create(head);
        return head;
    }
}