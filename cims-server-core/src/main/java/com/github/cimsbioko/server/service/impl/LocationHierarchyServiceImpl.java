package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.dao.LocationHierarchyRepository;
import com.github.cimsbioko.server.domain.LocationHierarchy;
import com.github.cimsbioko.server.service.LocationHierarchyService;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.sql.CallableStatement;
import java.sql.Types;

public class LocationHierarchyServiceImpl implements LocationHierarchyService {

    private EntityManager em;
    private LocationHierarchyRepository repo;

    public LocationHierarchyServiceImpl(EntityManager em, LocationHierarchyRepository repo) {
        this.em = em;
        this.repo = repo;
    }

    private Session getSession() {
        return em.unwrap(Session.class);
    }

    @Override
    @Transactional
    public LocationHierarchy createMap(String localityUuid, String mapUuid, String mapName) {
        String uuid = getSession().doReturningWork(
                c -> {
                    try (CallableStatement f = c.prepareCall("{ ? = call create_map(?, ?, ?) }")) {
                        f.registerOutParameter(1, Types.VARCHAR);
                        f.setString(2, localityUuid);
                        f.setString(3, mapUuid);
                        f.setString(4, mapName);
                        f.execute();
                        return f.getString(1);
                    }
                }
        );
        // FIXME: Use optional rather than null
        return repo.findById(uuid).orElse(null);
    }

    @Override
    @Transactional
    public LocationHierarchy createOrFindMap(String localityUuid, String mapName) {
        String uuid = getSession().doReturningWork(
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
        // FIXME: Use optional rather than null
        return repo.findById(uuid).orElse(null);
    }

    @Override
    @Transactional
    public LocationHierarchy createSector(String mapUuid, String sectorUuid, String sectorName) {
        String uuid = getSession().doReturningWork(
                c -> {
                    try (CallableStatement f = c.prepareCall("{ ? = call create_sector(?, ?, ?) }")) {
                        f.registerOutParameter(1, Types.VARCHAR);
                        f.setString(2, mapUuid);
                        f.setString(3, sectorUuid);
                        f.setString(4, sectorName);
                        f.execute();
                        return f.getString(1);
                    }
                }
        );
        // FIXME: Use optional rather than null
        return repo.findById(uuid).orElse(null);
    }

    @Override
    @Transactional
    public LocationHierarchy createOrFindSector(String mapUuid, String sectorName) {
        String uuid = getSession().doReturningWork(
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
        // FIXME: Use optional rather than null
        return repo.findById(uuid).orElse(null);
    }
}
