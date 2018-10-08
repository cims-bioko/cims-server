package com.github.cimsbioko.server.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

class AjaxResult {

    private List<String> messages = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    private Map<String, List<String>> fieldErrors = new HashMap<>();

    AjaxResult addMessage(String message) {
        messages.add(message);
        return this;
    }

    AjaxResult addError(String error) {
        errors.add(error);
        return this;
    }

    AjaxResult addFieldError(String field, String error) {
        fieldErrors.merge(field, singletonList(error),
                (a, b) -> Stream.concat(a.stream(), b.stream()).collect(toList()));
        return this;
    }

    public List<String> getMessages() {
        return messages;
    }

    public List<String> getErrors() {
        return errors;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
}
