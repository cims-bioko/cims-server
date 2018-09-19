package com.github.cimsbioko.server.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.util.Calendar;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
public class CalendarUtilTest {

    private CalendarUtil util;

    @Before
    public void setUp() {
        util = new CalendarUtil();
    }

    @Test
    public void testParseDateWithoutTime() throws ParseException {
        Calendar result = util.parse("2003-11-11");
        assertNotNull(result);
    }

    @Test
    public void testParseDateWithTime() throws ParseException {
        Calendar result = util.parse("2017-11-11 12:50:02");
        assertNotNull(result);
    }
}
