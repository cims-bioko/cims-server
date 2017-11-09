package com.github.cimsbioko.server.errorhandling.util;

import java.util.Calendar;
import java.util.List;

import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.domain.model.Error;

public class ErrorUtil {

    public static final int MAX_MESSAGE_LENGTH = 255;

    public static Error logError(String assignedTo, String dataPayload, Calendar dateOfResolution,
                                 String entityType, FieldWorker fieldWorker, String resolutionStatus, List<String> errors) {
        Error error = new Error();

        error.setAssignedTo(assignedTo);
        error.setDataPayload(dataPayload);
        error.setDateOfResolution(dateOfResolution);
        error.setEntityType(entityType);
        error.setFieldWorker(fieldWorker);
        error.setResolutionStatus(resolutionStatus);

        // abbreviate the message if it's too long
        String messageText = String.join(",", errors);
        if (messageText.length() > MAX_MESSAGE_LENGTH) {
            messageText = messageText.substring(0, MAX_MESSAGE_LENGTH - 1) + "…";
        }
        error.setErrorMessage(messageText);

        return error;
    }
}
