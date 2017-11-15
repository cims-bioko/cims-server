package com.github.cimsbioko.server.controller.service.impl;

import java.sql.SQLException;
import java.util.List;

import com.github.cimsbioko.server.controller.service.EntityService;
import com.github.cimsbioko.server.controller.service.IndividualService;
import com.github.cimsbioko.server.controller.service.RelationshipService;
import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.Membership;
import com.github.cimsbioko.server.domain.model.Relationship;
import com.github.cimsbioko.server.domain.model.SocialGroup;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.service.SitePropertiesService;
import org.springframework.transaction.annotation.Transactional;

public class RelationshipServiceImpl implements RelationshipService {

    private IndividualService individualService;
    private GenericDao genericDao;
    private SitePropertiesService siteProperties;
    private EntityService entityService;

    public RelationshipServiceImpl(GenericDao genericDao, EntityService entityService, IndividualService individualService, SitePropertiesService siteProperties) {
        this.genericDao = genericDao;
        this.entityService = entityService;
        this.individualService = individualService;
        this.siteProperties = siteProperties;
    }

    public Relationship evaluateRelationship(Relationship entityItem) throws ConstraintViolations {
        if (!checkValidRelationship(entityItem))
            throw new ConstraintViolations("An Individual cannot have multiple relationships with the same person.");
        if (individualService.getLatestEvent(entityItem.getIndividualA()).equals("Death") || individualService.getLatestEvent(entityItem.getIndividualB()).equals("Death"))
            throw new ConstraintViolations("A Relationship cannot be created for an Individual who has a Death event.");
        return entityItem;
    }

    public void validateGeneralRelationship(Relationship relationship) throws ConstraintViolations {
        if (!checkValidRelationship(relationship))
            throw new ConstraintViolations("An Individual cannot have multiple relationships with the same person.");
    }

    /**
     * Checks if a Relationship between Individual A and Individual B already exists.
     * Also checks if a Relationship between Individual B and A already exists.
     */
    public boolean checkValidRelationship(Relationship entityItem) {

        List<Relationship> itemsA = genericDao.findListByProperty(Relationship.class, "individualA", entityItem.getIndividualA());
        List<Relationship> itemsB = genericDao.findListByProperty(Relationship.class, "individualB", entityItem.getIndividualA());

        // compare lists
        for (Relationship relationA : itemsA) {
            if ((!relationA.equals(entityItem)) && (relationA.getIndividualB().getExtId().equals(entityItem.getIndividualB().getExtId())))
                return false;
        }
        for (Relationship relationB : itemsB) {
            if ((!relationB.equals(entityItem)) && (relationB.getIndividualA().getExtId().equals(entityItem.getIndividualB().getExtId())))
                return false;
        }
        return true;
    }

    public List<Relationship> getAllRelationships(Individual individual) {

        List<Relationship> itemsA = genericDao.findListByProperty(Relationship.class, "individualA", individual, true);
        List<Relationship> itemsB = genericDao.findListByProperty(Relationship.class, "individualB", individual, true);

        itemsA.addAll(itemsB);
        return itemsA;
    }

    public List<Relationship> getAllRelationshipsWithinSocialGroup(Individual individual, SocialGroup group) {

        List<Relationship> itemsA = genericDao.findListByProperty(Relationship.class, "individualA", individual);
        List<Relationship> itemsB = genericDao.findListByProperty(Relationship.class, "individualB", individual);

        itemsA.addAll(itemsB);

        for (int i = 0; i < itemsA.size(); i++) {
            boolean found = false;
            Relationship rel = itemsA.get(i);

            Individual indivA = rel.getIndividualA();
            Individual indivB = rel.getIndividualB();

            if (!group.getGroupHead().getExtId().equals(indivA.getExtId())) {
                List<Membership> memsA = genericDao.findListByProperty(Membership.class, "individual", indivA);

                for (Membership m : memsA) {
                    if (m.getSocialGroup().getExtId().equals(group.getExtId())) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    itemsA.remove(i);
            }

            if (!group.getGroupHead().getExtId().equals(indivB.getExtId())) {
                found = false;
                List<Membership> memsB = genericDao.findListByProperty(Membership.class, "individual", indivB);

                for (Membership m : memsB) {
                    if (m.getSocialGroup().getExtId().equals(group.getExtId())) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    itemsA.remove(i);
            }
        }
        return itemsA;
    }

    @Override
    public List<Relationship> getAllRelationships() {
        return genericDao.findAll(Relationship.class, true);
    }

    @Override
    @Transactional
    public void createRelationship(Relationship relationship) throws ConstraintViolations {
        evaluateRelationship(relationship);
        try {
            entityService.create(relationship);
        } catch (IllegalArgumentException e) {
        } catch (SQLException e) {
            throw new ConstraintViolations("There was a problem saving the relationship to the database");
        }
    }

    @Override
    @Authorized("VIEW_ENTITY")
    public List<Relationship> getAllRelationshipInRange(Relationship start, int pageSize) {
        Object startProp = start == null ? null : start.getUuid();
        return genericDao.findPaged(Relationship.class, "id", startProp, pageSize);
    }

    @Override
    @Authorized("VIEW_ENTITY")
    public long getTotalRelationshipCount() {
        return genericDao.getTotalCount(Relationship.class);
    }
}
