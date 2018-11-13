package com.github.cimsbioko.server.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Calendar;

/**
 * An adapter for converting between String and Calendar types when posting
 * to a restful web service. Jax-rs requires custom marshalling if the field
 * cannot be mapped.
 */
public class CalendarAdapter extends XmlAdapter<String, Calendar> {

    private CalendarUtil util;

    public CalendarAdapter(CalendarUtil util) {
        this.util = util;
    }

    public String marshal(Calendar v) {
        return util.formatDateTime(v);
    }

    public Calendar unmarshal(String v) throws Exception {
        return util.parse(v);
    }
}
