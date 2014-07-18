package org.openhds.webservice.resources.bioko;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.webservice.WebServiceCallException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class AbstractFormResource {

    protected ResponseEntity<WebServiceCallException> requestError(String message) {
        WebServiceCallException error = new WebServiceCallException();
        error.getErrors().add(message);
        return new ResponseEntity<WebServiceCallException>(error, HttpStatus.BAD_REQUEST);
    }

    protected ResponseEntity<WebServiceCallException> serverError(String message) {
        WebServiceCallException error = new WebServiceCallException();
        error.getErrors().add(message);
        return new ResponseEntity<WebServiceCallException>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected ResponseEntity<WebServiceCallException> requestError(ConstraintViolations cv) {
        return new ResponseEntity<WebServiceCallException>(new WebServiceCallException(cv),
                HttpStatus.BAD_REQUEST);
    }

}
