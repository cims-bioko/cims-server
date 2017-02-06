package com.github.cimsbioko.server.errorhandling.dao;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import com.github.cimsbioko.server.controller.service.EntityService;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.ErrorLog;
import com.github.cimsbioko.server.domain.util.CalendarUtil;
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