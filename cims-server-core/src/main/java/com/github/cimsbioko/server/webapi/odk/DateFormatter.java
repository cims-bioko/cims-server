package com.github.cimsbioko.server.webapi.odk;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

public interface DateFormatter {

    String formatSubmitDate(Date date);

    Timestamp parseSubmitDate(String s) throws ParseException;

    String formatCollectionDate(Date date);
}
