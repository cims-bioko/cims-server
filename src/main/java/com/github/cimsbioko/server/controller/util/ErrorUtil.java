package com.github.cimsbioko.server.controller.util;

import java.util.List;

import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.domain.model.Error;

public class ErrorUtil {

    public static final int MAX_MESSAGE_LENGTH = 255;

    public static Error createError(String dataPayload, String entityType, FieldWorker fieldWorker, List<String> errors) {
        Error error = new Error();
        error.setPayload(dataPayload);
        error.setEntityType(entityType);
        error.setFieldWorker(fieldWorker);
        // abbreviate the message if it's too long
        String messageText = String.join(",", errors);
        if (messageText.length() > MAX_MESSAGE_LENGTH) {
            messageText = messageText.substring(0, MAX_MESSAGE_LENGTH - 1) + "…";
        }
        error.setMessage(messageText);
        return error;
    }
}
