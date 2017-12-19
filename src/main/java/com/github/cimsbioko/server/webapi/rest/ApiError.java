package com.github.cimsbioko.server.webapi.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.github.cimsbioko.server.exception.ConstraintViolations;

@XmlRootElement(name = "failure")
public class ApiError implements Serializable {

    private static final long serialVersionUID = -5429038867393148120L;
    private List<String> errors = new ArrayList<>();

    public ApiError() {
    }

    public ApiError(ConstraintViolations violations) {
        for (String violation : violations.getViolations()) {
            errors.add(violation);
        }
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
