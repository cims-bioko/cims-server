package com.github.cimsbioko.server.webapi;

import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.errorhandling.constants.ErrorConstants;
import com.github.cimsbioko.server.errorhandling.service.ErrorHandlingService;
import com.github.cimsbioko.server.errorhandling.util.ErrorLogUtil;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.ErrorLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.xml.bind.Marshaller;
import java.util.Calendar;

public class AbstractFormResource {

    public static final String NOT_APPLICABLE_END_TYPE = "NA";

    @Autowired
    private ErrorHandlingService errorService;

    private Marshaller marshaller;

    protected ResponseEntity<WebServiceCallException> requestError(String message) {
        WebServiceCallException error = new WebServiceCallException();
        error.getErrors().add(message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    protected ResponseEntity<WebServiceCallException> serverError(String message) {
        WebServiceCallException error = new WebServiceCallException();
        error.getErrors().add(message);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected ResponseEntity<WebServiceCallException> requestError(ConstraintViolations cv) {
        return new ResponseEntity<>(new WebServiceCallException(cv),
                HttpStatus.BAD_REQUEST);
    }

    protected void logError(ConstraintViolations cv, FieldWorker fw, String payload, String simpleClassName, String errorConstant) {

        ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, payload, null, simpleClassName,
                fw, errorConstant, cv.getViolations());
        errorService.logError(error);

    }

    protected static Calendar getDateInPast() {
        Calendar inPast = Calendar.getInstance();
        inPast.set(1900, Calendar.JANUARY, 1);
        return inPast;
    }

}
