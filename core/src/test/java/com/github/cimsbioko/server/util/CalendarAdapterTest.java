package com.github.cimsbioko.server.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.util.Calendar;

import static org.junit.Assert.*;

public class CalendarAdapterTest {

    private CalendarAdapter calendarAdapter;
    private Calendar dateTime;
    private Calendar date;
    private CalendarUtil util;

    private static final String MOCK_DATETIME = "2000-06-13 12:12:12";
    private static final String MOCK_DATE = "2000-06-13";

    @Before
    public void setup() throws ParseException {
        util = new CalendarUtil();
        calendarAdapter = new CalendarAdapter(util);
        date = getMockDate();
        dateTime = getMockDateTime();
    }

    private Calendar getMockDate() throws ParseException {
        return util.parseDate(MOCK_DATE);
    }

    private Calendar getMockDateTime() throws ParseException {
        return util.parseDateTime(MOCK_DATETIME);
    }

    @Test
    public void marshalDateTime() {
        String marshalledCalendar = null;
        try {
            marshalledCalendar = calendarAdapter.marshal(dateTime);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertNotNull(marshalledCalendar);
        assertEquals(MOCK_DATETIME, marshalledCalendar);
    }

    @Test
    public void unmarshalDateTime() {
        Calendar unmarshalledCalendar = null;
        try {
            unmarshalledCalendar = calendarAdapter.unmarshal(MOCK_DATETIME);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertNotNull(unmarshalledCalendar);
        assertEquals(unmarshalledCalendar.getTime(), dateTime.getTime());
    }

    @Test
    public void marshalDate() {
        String marshalledCalendar = null;
        try {
            marshalledCalendar = calendarAdapter.marshal(date);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertNotNull(marshalledCalendar);
        assertEquals(MOCK_DATE, marshalledCalendar.substring(0, 10));
    }

    @Test
    public void unmarshalDate() {
        Calendar unmarshalledCalendar = null;
        try {
            unmarshalledCalendar = calendarAdapter.unmarshal(MOCK_DATE);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertNotNull(unmarshalledCalendar);
        assertEquals(date.getTime(), unmarshalledCalendar.getTime());
    }
}
