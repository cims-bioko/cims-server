package com.github.cimsbioko.server.integration.bioko;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.github.cimsbioko.server.domain.util.CalendarAdapter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:domainTestContext.xml"})
public class CalendarAdapterTest {

    private CalendarAdapter calendarAdapter;
    private Calendar dateTime;
    private Calendar date;

    private static final String SQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    private static final String MOCK_DATETIME = "2000-06-13 12:12:12";
    private static final String MOCK_DATE = "2000-06-13";

    @Before
    public void setup() throws ParseException {

        calendarAdapter = new CalendarAdapter();
        date = getMockDate();
        dateTime = getMockDateTime();
    }

    private Calendar getMockDate() throws ParseException {

        DateFormat formatter = new SimpleDateFormat(SQL_DATE_FORMAT);
        Date date = formatter.parse(MOCK_DATE);
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        return dateCal;
    }

    private Calendar getMockDateTime() throws ParseException {

        DateFormat formatter = new SimpleDateFormat(SQL_DATETIME_FORMAT);
        Date date = formatter.parse(MOCK_DATETIME);
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        return dateCal;
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
