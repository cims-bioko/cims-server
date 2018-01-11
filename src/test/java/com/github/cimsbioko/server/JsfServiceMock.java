package com.github.cimsbioko.server;

import java.util.ArrayList;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;

import com.github.cimsbioko.server.web.service.JsfService;

/**
 * A special JsfService Mock implementation to be used only for testing.
 * It strips the dependencies on the the FacesContext.
 */
public class JsfServiceMock implements JsfService {

    List<String> errors = new ArrayList<>();

    @Override
    public void addError(String msg) {
        errors.add(msg);
    }

    @Override
    public String getLocalizedString(String key) {
        return "whatever";
    }

    @Override
    public Object getObjViaReqParam(String reqParam, Converter converter, UIComponent component) {
        return null;
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
