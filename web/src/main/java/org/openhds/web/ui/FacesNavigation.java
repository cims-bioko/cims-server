package org.openhds.web.ui;

import java.io.IOException;
import java.net.URL;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.context.ExternalContext;
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

    public void redirect(String event) throws IOException {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extCtx = ctx.getExternalContext();
        URL absUrl = new URL(extCtx.getRequestScheme(),
                extCtx.getRequestServerName(),
                extCtx.getRequestServerPort(),
                extCtx.getRequestContextPath() + lookup(event));
        extCtx.redirect(absUrl.toString());
    }

    public String getNavigateTo() {
        return lookup(eventId);
    }
}
