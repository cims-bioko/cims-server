package com.github.cimsbioko.server.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class TimeUtil {

    private static final Map<String, Long> TIME_UNITS = new LinkedHashMap<>();

    static {
        TIME_UNITS.put("w", 7 * 24 * 60 * 60 * 1000L);
        TIME_UNITS.put("d", 24 * 60 * 60 * 1000L);
        TIME_UNITS.put("h", 60 * 60 * 1000L);
        TIME_UNITS.put("m", 60 * 1000L);
        TIME_UNITS.put("s", 1000L);
        TIME_UNITS.put("ms", 1L);
    }

    public static String describeDuration(long durationInMillis) {
        StringBuilder s = new StringBuilder();
        long quotient, remainder = durationInMillis;
        for (Map.Entry<String, Long> unitsEntry : TIME_UNITS.entrySet()) {
            if (remainder > 0) {
                long divisor = unitsEntry.getValue();
                String unit = unitsEntry.getKey();
                quotient = remainder / divisor;
                if (quotient > 0) {
                    s.append(quotient);
                    s.append(unit);
                }
                remainder = remainder % divisor;
            } else {
                break;
            }
        }
        return s.toString();
    }
}
