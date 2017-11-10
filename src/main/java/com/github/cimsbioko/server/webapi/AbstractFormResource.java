package com.github.cimsbioko.server.webapi;

import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.errorhandling.Constants;
import com.github.cimsbioko.server.errorhandling.ErrorService;
import com.github.cimsbioko.server.errorhandling.ErrorUtil;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Calendar;

public class AbstractFormResource {

    public static final String NOT_APPLICABLE_END_TYPE = "NA";

    @Autowired
    private ErrorService errorService;

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

    protected void logError(ConstraintViolations cv, FieldWorker fw, String payload, String simpleClassName, String errorConstant) {

        Error error = ErrorUtil.createError(Constants.UNASSIGNED, payload, null, simpleClassName,
                fw, errorConstant, cv.getViolations());
        errorService.logError(error);

    }

    protected static Calendar getDateInPast() {
        Calendar inPast = Calendar.getInstance();
        inPast.set(1900, Calendar.JANUARY, 1);
        return inPast;
    }

}
