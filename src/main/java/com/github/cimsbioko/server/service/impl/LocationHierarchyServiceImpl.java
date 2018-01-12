package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.service.EntityService;
import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.idgen.Generator;
import com.github.cimsbioko.server.service.LocationHierarchyService;
import com.github.cimsbioko.server.domain.Location;
import com.github.cimsbioko.server.domain.LocationHierarchy;
import com.github.cimsbioko.server.domain.LocationHierarchyLevel;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

@SuppressWarnings("unchecked")
public class LocationHierarchyServiceImpl implements LocationHierarchyService {

    private GenericDao genericDao;
    private Generator locationGenerator;
    private Generator locationHierarchyGenerator;
    private EntityService entityService;
    private SessionFactory sessionFactory;

    public LocationHierarchyServiceImpl(GenericDao genericDao, EntityService entityService, Generator locationGenerator,
                                        Generator locationHierarchyGenerator, SessionFactory sessionFactory) {
        this.genericDao = genericDao;
        this.entityService = entityService;
        this.locationGenerator = locationGenerator;
        this.locationHierarchyGenerator = locationHierarchyGenerator;
        this.sessionFactory = sessionFactory;
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

    @Override
    public LocationHierarchy createOrFindMap(String localityUuid, String mapName) {
        String uuid = sessionFactory.getCurrentSession().doReturningWork(
                c -> {
                    try (CallableStatement f = c.prepareCall("{ ? = call create_map(?, ?) }")) {
                        f.registerOutParameter(1, Types.VARCHAR);
                        f.setString(2, localityUuid);
                        f.setString(3, mapName);
                        f.execute();
                        return f.getString(1);
                    }
                }
        );
        return entityService.read(LocationHierarchy.class, uuid);
    }

    @Override
    public LocationHierarchy createOrFindSector(String mapUuid, String sectorName) {
        String uuid = sessionFactory.getCurrentSession().doReturningWork(
                c -> {
                    try (CallableStatement f = c.prepareCall("{ ? = call create_sector(?, ?) }")) {
                        f.registerOutParameter(1, Types.VARCHAR);
                        f.setString(2, mapUuid);
                        f.setString(3, sectorName);
                        f.execute();
                        return f.getString(1);
                    }
                }
        );
        return entityService.read(LocationHierarchy.class, uuid);
    }

    public LocationHierarchy generateId(LocationHierarchy entityItem) throws ConstraintViolations {
        entityItem.setExtId(locationHierarchyGenerator.generateId(entityItem));
        return entityItem;
    }

    public LocationHierarchyLevel getLevel(int level) {
        return genericDao.findByProperty(LocationHierarchyLevel.class, "keyIdentifier", level);
    }
}