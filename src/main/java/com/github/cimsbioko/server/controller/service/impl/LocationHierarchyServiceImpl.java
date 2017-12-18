package com.github.cimsbioko.server.controller.service.impl;

import com.github.cimsbioko.server.controller.service.EntityService;
import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.controller.idgen.Generator;
import com.github.cimsbioko.server.controller.service.LocationHierarchyService;
import com.github.cimsbioko.server.domain.model.Location;
import com.github.cimsbioko.server.domain.model.LocationHierarchy;
import com.github.cimsbioko.server.domain.model.LocationHierarchyLevel;

import java.sql.SQLException;

@SuppressWarnings("unchecked")
public class LocationHierarchyServiceImpl implements LocationHierarchyService {

    private GenericDao genericDao;
    private Generator locationGenerator;
    private Generator locationHierarchyGenerator;
    private EntityService entityService;

    public LocationHierarchyServiceImpl(GenericDao genericDao, EntityService entityService, Generator locationGenerator,
                                        Generator locationHierarchyGenerator) {
        this.genericDao = genericDao;
        this.entityService = entityService;
        this.locationGenerator = locationGenerator;
        this.locationHierarchyGenerator = locationHierarchyGenerator;
    }

    public LocationHierarchy findByUuid(String uuid) {
        return genericDao.findByProperty(LocationHierarchy.class, "uuid", uuid);
    }

    public LocationHierarchy findByExtId(String extId) {
        return genericDao.findByProperty(LocationHierarchy.class, "extId", extId);
    }

    public void createLocationHierarchy(LocationHierarchy locationHierarchy) throws ConstraintViolations {
        try {
            entityService.create(locationHierarchy);
        } catch (IllegalArgumentException e) {
            throw new ConstraintViolations("IllegalArgumentException saving the location hierarchy to the database: " + e.getMessage());
        } catch (SQLException e) {
            throw new ConstraintViolations("SQLException saving the location hierarchy to the database: " + e.getMessage());
        }
    }

    public Location generateId(Location entityItem) throws ConstraintViolations {
        entityItem.setExtId(locationGenerator.generateId(entityItem));
        return entityItem;
    }

    public LocationHierarchy generateId(LocationHierarchy entityItem) throws ConstraintViolations {
        entityItem.setExtId(locationHierarchyGenerator.generateId(entityItem));
        return entityItem;
    }

    public LocationHierarchyLevel getLevel(int level) {
        return genericDao.findByProperty(LocationHierarchyLevel.class, "keyIdentifier", level);
    }
}