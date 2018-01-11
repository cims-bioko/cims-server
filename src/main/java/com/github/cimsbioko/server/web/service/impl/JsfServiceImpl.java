package com.github.cimsbioko.server.web.service.impl;

import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;

import com.github.cimsbioko.server.web.service.JsfService;

public class JsfServiceImpl implements JsfService {
    // this field was added because jsf 1.2 does not support the
    // the f:param element within commandButtons
    // Instead, need to use an alternative method which requires
    // a field on a bean
    // refer to: http://forums.sun.com/thread.jspa?threadID=5366506
    String itemId;

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

    public void addError(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                msg, msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    }
}
