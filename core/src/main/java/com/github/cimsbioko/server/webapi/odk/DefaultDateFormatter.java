package com.github.cimsbioko.server.webapi.odk;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DefaultDateFormatter implements DateFormatter {

    private static final String ODK_SUBMIT_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    private static final String COLLECTION_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";


    public String formatSubmitDate(Date d) {
        if (d != null) {
            return new SimpleDateFormat(ODK_SUBMIT_DATE_PATTERN).format(d);
        }
        return "";
    }

    public Timestamp parseSubmitDate(String s) throws ParseException {
        if (s != null) {
            return new Timestamp(new SimpleDateFormat(ODK_SUBMIT_DATE_PATTERN).parse(s).getTime());
        }
        return null;
    }

    @Override
    public String formatCollectionDate(Date date) {
        return new SimpleDateFormat(COLLECTION_DATE_PATTERN).format(date);
    }
}
