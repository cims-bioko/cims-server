package com.github.cimsbioko.server.util;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * An adapter for converting between String and Calendar types when posting
 * to a restful web service. Jax-rs requires custom marshalling if the field
 * cannot be mapped.
 */
@Component
public class CalendarAdapter extends XmlAdapter<String, Calendar> {

    @Autowired
    private CalendarUtil util;

    private void init() {
        if (util == null) {
            util = new CalendarUtil();
        }
    }

    public String marshal(Calendar v) {
        init();
        return util.formatDateTime(v);
    }

    public Calendar unmarshal(String v) throws Exception {
        init();
        return util.parse(v);
    }
}
