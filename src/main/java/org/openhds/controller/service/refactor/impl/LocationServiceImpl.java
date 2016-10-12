package org.openhds.controller.service.refactor.impl;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.refactor.LocationService;
import org.openhds.controller.service.refactor.crudhelpers.EntityCrudHelper;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    @Qualifier("LocationCrudHelper")
    private EntityCrudHelper<Location> locationCrudHelper;

    @Autowired
    private GenericDao genericDao;

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
        boolean nullLocationHierarchy = (null == location.getLocationHierarchy());

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
