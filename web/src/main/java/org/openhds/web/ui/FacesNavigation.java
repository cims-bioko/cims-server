package org.openhds.web.ui;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.context.FacesContext;

/**
 * A utility for determining the view for a given outcome based on JSF config.
 */
public class FacesNavigation {

    public static final String GLOBAL = "*";

    private String eventId;

    public boolean setNavigateTo(String eventId) {
        this.eventId = eventId;
        return true;
    }

    public String getNavigateTo() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) ctx.getApplication().getNavigationHandler();
        NavigationCase navCase = handler.getNavigationCase(ctx, null, eventId);
        return navCase != null ? navCase.getToViewId(ctx) : eventId;
    }
}
