package com.github.cimsbioko.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarUtil {

    private static final Logger log = LoggerFactory.getLogger(CalendarUtil.class);

    private String dateFormat;
    private String dateTimeFormat;

    public CalendarUtil() {
        dateFormat = "yyyy-MM-dd";
        dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    }

    public CalendarUtil(String dateFormat, String dateTimeFormat) {
        this.dateFormat = dateFormat;
        this.dateTimeFormat = dateTimeFormat;
    }

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
