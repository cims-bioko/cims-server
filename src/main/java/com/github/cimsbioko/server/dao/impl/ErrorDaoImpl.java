package com.github.cimsbioko.server.dao.impl;

import java.sql.SQLException;
import java.util.Date;

import com.github.cimsbioko.server.service.EntityService;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.dao.ErrorDao;
import com.github.cimsbioko.server.domain.Error;
import com.github.cimsbioko.server.util.CalendarUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("errorDao")
public class ErrorDaoImpl implements ErrorDao {

    private static final Logger logger = LoggerFactory.getLogger(ErrorDaoImpl.class);

    @Autowired
    private EntityService entityService;

    @Autowired
    private CalendarUtil calendarUtil;

    public Error save(Error error) {
        try {
            error.setCreated(calendarUtil.dateToCalendar(new Date()));
            entityService.create(error);
        } catch (IllegalArgumentException | SQLException | ConstraintViolations e) {
            logger.warn("Unable to insert error log into DB: " + e.getMessage());
        }

        return error;
    }
}
