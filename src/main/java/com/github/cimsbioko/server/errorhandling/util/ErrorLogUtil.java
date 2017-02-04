package com.github.cimsbioko.server.errorhandling.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.domain.model.Error;
import com.github.cimsbioko.server.domain.model.ErrorLog;

public class ErrorLogUtil {

    public static ErrorLog generateErrorLog(String assignedTo, String dataPayload, Calendar dateOfResolution,
                                            String entityType, FieldWorker fieldWorker, String resolutionStatus, List<String> errors) {
        ErrorLog errorLog = new ErrorLog();

        errorLog.setAssignedTo(assignedTo);
        errorLog.setDataPayload(dataPayload);
        errorLog.setDateOfResolution(dateOfResolution);
        errorLog.setEntityType(entityType);
        errorLog.setFieldWorker(fieldWorker);
        errorLog.setResolutionStatus(resolutionStatus);
        errorLog.setErrors(generateErrors(errors));

        return errorLog;
    }

    private static List<Error> generateErrors(List<String> errors) {
        List<Error> objectErrors = new ArrayList<>();

        for (String error : errors) {
            Error objectError = new Error(error);
            objectErrors.add(objectError);
        }

        return objectErrors;
    }
}
