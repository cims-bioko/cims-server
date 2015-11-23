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

    public String lookup(String event) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) ctx.getApplication().getNavigationHandler();
        NavigationCase navCase = handler.getNavigationCase(ctx, null, event);
        if (navCase != null) {
            String viewId = navCase.getToViewId(ctx);
            return viewId.replaceFirst("[.]xhtml$", ".faces");  // translate proper view-ids to faces URLs
        }
        return event;
    }

    public String getNavigateTo() {
        return lookup(eventId);
    }
}
