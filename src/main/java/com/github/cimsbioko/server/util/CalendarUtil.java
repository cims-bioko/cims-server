package com.github.cimsbioko.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Component
public class CalendarUtil {

    private static final Logger log = LoggerFactory.getLogger(CalendarUtil.class);

    @Value("${dateFormat:yyyy-MM-dd}")
    private String dateFormat = "yyyy-MM-dd";

    @Value("${dateTimeFormat:yyyy-MM-dd HH:mm:ss}")
    private String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    private Calendar strictParse(String dateString, String format) throws ParseException {
        DateFormat formatter = new SimpleDateFormat(format);
        formatter.setLenient(false);
        Date date = formatter.parse(dateString);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public Calendar parseDateTime(String dateStr) throws ParseException {
        log.debug("parsing datetime {}", dateStr);
        return strictParse(dateStr, dateTimeFormat);
    }

    public Calendar parseDate(String dateStr) throws ParseException {
        log.debug("parsing date {}", dateStr);
        return strictParse(dateStr, dateFormat);
    }

    public Calendar parse(String dateStr) throws ParseException {
        try {
            return parseDateTime(dateStr);
        } catch (ParseException pe) {
            try {
                return parseDate(dateStr);
            } catch (ParseException pe2) {
                log.debug("failed to parse date", pe2);
                throw pe2;
            }
        }
    }

    public Calendar dateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public String formatDateTime(Calendar calendar) {
        return new SimpleDateFormat(dateTimeFormat).format(calendar.getTime());
    }
}
