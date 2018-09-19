package com.github.cimsbioko.server.service.refactor.crudhelpers;

import com.github.cimsbioko.server.domain.Location;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.idgen.LocationGenerator;
import com.github.cimsbioko.server.service.refactor.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("LocationCrudHelper")
public class LocationCrudHelper extends AbstractEntityCrudHelperImpl<Location> {

    @Autowired
    LocationService locationService;

    @Autowired
    LocationGenerator locationGenerator;

    @Override
    protected void preCreateSanityChecks(Location location) {
    }

    @Override
    protected void cascadeReferences(Location location) throws ConstraintViolations {
        if (null == location.getExtId()) {
            locationGenerator.generateId(location);
        }
    }

    @Override
    protected void validateReferences(Location location) throws ConstraintViolations {
        ConstraintViolations constraintViolations = new ConstraintViolations();
        if (!locationService.isEligibleForCreation(location, constraintViolations)) {
            throw (constraintViolations);
        }
    }

    @Override
    public List<Location> getAll() {
        return genericDao.findAll(Location.class, true);
    }

    @Override
    public Location getByExtId(String id) {
        return genericDao.findByProperty(Location.class, "extId", id, true);
    }

    @Override
    public Location getByUuid(String id) {
        return genericDao.findByProperty(Location.class, "uuid", id);
    }

}
