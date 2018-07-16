package com.github.cimsbioko.server.web.service;

import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;

public interface JsfService {

    SelectItem[] getSelectItems(List<?> entities);

    String getReqParam(String name);

    Object getObjViaReqParam(String reqParam, Converter converter, UIComponent component);

    void addError(String key, Object... params);

    void addMessage(String key, Object... params);

    String getMessage(String key, Object... params);

}
