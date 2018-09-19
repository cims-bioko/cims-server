package com.github.cimsbioko.server;

import com.github.cimsbioko.server.web.service.JsfService;

import javax.faces.component.UIComponent;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

/**
 * A special JsfService Mock implementation to be used only for testing.
 * It strips the dependencies on the the FacesContext.
 */
public class JsfServiceMock implements JsfService {

    List<String> errors = new ArrayList<>();

    @Override
    public Object getObjViaReqParam(String reqParam, Converter converter, UIComponent component) {
        return null;
    }

    @Override
    public void addError(String key, Object... params) {
    }

    @Override
    public void addMessage(String key, Object... params) {
    }

    @Override
    public String getMessage(String key, Object... params) {
        return "Whatever";
    }

    @Override
    public String getReqParam(String name) {
        return null;
    }

    @Override
    public SelectItem[] getSelectItems(List<?> entities) {
        return null;
    }

}
