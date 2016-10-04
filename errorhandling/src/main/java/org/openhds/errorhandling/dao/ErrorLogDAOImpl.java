package org.openhds.errorhandling.dao;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.EntityService;
import org.openhds.domain.model.ErrorLog;
import org.openhds.domain.util.CalendarUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ErrorLogDAOImpl implements ErrorLogDAO {

    private static final Logger logger = LoggerFactory.getLogger(ErrorLogDAOImpl.class);
    private EntityService entityService;

    @Autowired
    private CalendarUtil calendarUtil;

    public ErrorLogDAOImpl(EntityService entityService) {
        this.entityService = entityService;
    }

    public ErrorLog createErrorLog(ErrorLog error) {
        try {
            Calendar insertDate = calendarUtil.convertDateToCalendar(new Date());
            error.setInsertDate(insertDate);
            entityService.create(error);
        } catch (IllegalArgumentException | SQLException | ConstraintViolations e) {
            logger.warn("Unable to insert error log into DB: " + e.getMessage());
        }

        return error;
    }
}
