package com.github.cimsbioko.server.web.cvt;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeStampConverter implements Converter {

    private Format formatter;

    public TimeStampConverter(String dateFormat) {
        formatter = new SimpleDateFormat(dateFormat);
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return formatter.format(((Calendar) value).getTime());
    }

}
