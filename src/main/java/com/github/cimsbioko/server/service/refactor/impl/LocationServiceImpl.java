package com.github.cimsbioko.server.service.refactor.impl;

import com.github.cimsbioko.server.service.refactor.LocationService;
import com.github.cimsbioko.server.service.refactor.crudhelpers.EntityCrudHelper;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    @Qualifier("LocationCrudHelper")
    private EntityCrudHelper<Location> locationCrudHelper;

    @Override
    public List<Location> getAll() {
        return locationCrudHelper.getAll();
    }

    @Override
    public Location getByExtId(String id) {
        return locationCrudHelper.getByExtId(id);
    }

    @Override
    public Location getByUuid(String id) {
        return locationCrudHelper.getByUuid(id);
    }

    @Override
    public boolean isEligibleForCreation(Location location, ConstraintViolations cv) {

        if (null == location) {
            ConstraintViolations.addViolationIfNotNull(cv, "Null location.");
            return false;
        }

        boolean nullExtId = (null == location.getExtId());
        boolean nullLocationHierarchy = (null == location.getHierarchy());

        if (nullExtId) {
            ConstraintViolations.addViolationIfNotNull(cv, "The location has a null ExtId.");
        }
        if (nullLocationHierarchy) {
            ConstraintViolations.addViolationIfNotNull(cv, "The location has a null nullLocationHierarchy.");
        }

        return !nullExtId && !nullLocationHierarchy;
    }

    @Override
    public void delete(Location location) throws IllegalArgumentException {
        locationCrudHelper.delete(location);
    }

    @Override
    public void create(Location location) throws ConstraintViolations {
        locationCrudHelper.create(location);
    }

    @Override
    public void save(Location location) throws ConstraintViolations {
        locationCrudHelper.save(location);
    }
}
