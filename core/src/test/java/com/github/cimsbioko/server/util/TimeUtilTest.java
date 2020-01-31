package com.github.cimsbioko.server.util;

import org.junit.Test;

import static com.github.cimsbioko.server.util.TimeUtil.describeDuration;
import static org.junit.Assert.assertEquals;

public class TimeUtilTest {

    @Test
    public void describeDurationMillis() {
        assertEquals("5ms", describeDuration(5));
    }

    @Test
    public void describeDurationSeconds() {
        assertEquals("5s", describeDuration(5 * 1000));
    }

    @Test
    public void describeDurationMinutes() {
        assertEquals("5m", describeDuration(5 * 60 * 1000));
    }

    @Test
    public void describeDurationHours() {
        assertEquals("5h", describeDuration(5 * 60 * 60 * 1000));
    }

    @Test
    public void describeDurationDays() {
        assertEquals("5d", describeDuration(5 * 24 * 60 * 60 * 1000));
    }

    @Test
    public void describeDurationWeeks() {
        assertEquals("5w", describeDuration(5 * 7 * 24 * 60 * 60 * 1000L));
    }

    @Test
    public void describeDurationMixed() {
        assertEquals("1w3d17h34m13s323ms", describeDuration(
                (7 * 24 * 60 * 60 * 1000) + // 1 week
                (3 * 24 * 60 * 60 * 1000) + // 3 days
                (17 * 60 * 60 * 1000) + // 17 hours
                (34 * 60 * 1000) + // 34 minutes
                (13 * 1000) + // 13 seconds
                323 // 323 milliseconds
        ));
    }

    @Test
    public void describeDurationMixedWithHoles() {
        assertEquals("1w17h13s", describeDuration(
                (7 * 24 * 60 * 60 * 1000) + // 1 week
                (17 * 60 * 60 * 1000) + // 17 hours
                (13 * 1000) // 13 seconds
        ));
    }
}
