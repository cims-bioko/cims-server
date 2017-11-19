package com.github.cimsbioko.server.webapi.rest;

import com.github.cimsbioko.server.controller.service.ErrorService;
import com.github.cimsbioko.server.controller.util.ErrorUtil;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Error;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Calendar;

public class AbstractFormResource {

    @Autowired
    private ErrorService errorService;

    @Autowired
    private GeometryFactory geometryFactory;

    protected ResponseEntity<ApiError> requestError(String message) {
        ApiError error = new ApiError();
        error.getErrors().add(message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    protected ResponseEntity<ApiError> serverError(String message) {
        ApiError error = new ApiError();
        error.getErrors().add(message);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected ResponseEntity<ApiError> requestError(ConstraintViolations cv) {
        return new ResponseEntity<>(new ApiError(cv),
                HttpStatus.BAD_REQUEST);
    }

    protected void logError(ConstraintViolations cv, String payload, String simpleClassName) {
        Error error = ErrorUtil.createError( payload, simpleClassName, cv.getViolations());
        errorService.logError(error);
    }

    protected static Calendar getDateInPast() {
        Calendar inPast = Calendar.getInstance();
        inPast.set(1900, Calendar.JANUARY, 1);
        return inPast;
    }

    protected Point makePoint(String longitude, String latitude) {
        Double lng = Double.parseDouble(longitude), lat = Double.parseDouble(latitude);
        Coordinate coord = new Coordinate(lng, lat);
        return geometryFactory.createPoint(coord);
    }
}