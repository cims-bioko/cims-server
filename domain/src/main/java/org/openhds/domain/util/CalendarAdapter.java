package org.openhds.domain.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * An adapter for converting between String and Calendar types when posting
 * to a restful webservice. Jax-rs requires custom marshalling if the field 
 * cannot be mapped. 
 */
@Component
public class CalendarAdapter extends XmlAdapter<String, Calendar> {

    private final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    @Autowired
    private CalendarUtil calendarUtil;

    public String marshal(Calendar v) throws Exception {

        if (null == calendarUtil) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(SQL_DATE_FORMAT);
            return dateFormat.format(v.getTime());
        }

        return calendarUtil.formatDate(v);
    }

    public Calendar unmarshal(String v) throws Exception {

        if (null == calendarUtil) {
            DateFormat formatter = new SimpleDateFormat(SQL_DATE_FORMAT);
            formatter.setLenient(false);
            Date date = formatter.parse(v);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        }

        return calendarUtil.parseDate(v);
    }
}
