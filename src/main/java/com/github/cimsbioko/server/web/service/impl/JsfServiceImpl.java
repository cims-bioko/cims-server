package com.github.cimsbioko.server.web.service.impl;

import com.github.cimsbioko.server.web.service.JsfService;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import java.text.MessageFormat;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class JsfServiceImpl implements JsfService {

    // this field was added because jsf 1.2 does not support the
    // the f:param element within commandButtons
    // Instead, need to use an alternative method which requires
    // a field on a bean
    // refer to: http://forums.sun.com/thread.jspa?threadID=5366506
    private String itemId;

    public SelectItem[] getSelectItems(List<?> entities) {
        int size = entities.size();
        SelectItem[] items = new SelectItem[size];
        int i = 0;
        for (Object x : entities) {
            items[i++] = new SelectItem(x, x.toString());
        }
        return items;
    }

    public String getReqParam(String name) {
        String value = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get(name);

        if (value == null) {
            return itemId;
        }

        return value;
    }

    public Object getObjViaReqParam(String reqParam, Converter converter,
                                    UIComponent component) {
        String theId = getReqParam(reqParam);
        return converter.getAsObject(FacesContext.getCurrentInstance(),
                component, theId);
    }

    public void addError(String key, Object... params) {
        FacesMessage facesMsg = getFacesMessage(key, params, FacesMessage.SEVERITY_ERROR);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public void addMessage(String key, Object... params) {
        FacesMessage facesMsg = getFacesMessage(key, params, FacesMessage.SEVERITY_INFO);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    private FacesMessage getFacesMessage(String key, Object[] params, FacesMessage.Severity severityInfo) {
        String msg = getMessage(key, params);
        return new FacesMessage(severityInfo, msg, msg);
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    private FacesContext getContext() {
        return FacesContext.getCurrentInstance();
    }

    private ResourceBundle getBundle() {
        return getContext().getApplication().getResourceBundle(FacesContext.getCurrentInstance(), "msg");
    }

    public String getMessage(String key, Object... params) {

        String text;

        try {
            text = getBundle().getString(key);
        } catch (MissingResourceException e) {
            text = "??" + key + "??";
        }

        if (params != null) {
            MessageFormat mf = new MessageFormat(text);
            text = mf.format(params, new StringBuffer(), null).toString();
        }

        return text;
    }
}
