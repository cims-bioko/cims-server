package org.openhds.web.ui;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ConfigNavigationCase;

import org.springframework.web.context.ServletContextAware;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 * A utility for determining the view for a given outcome based on JSF config.
 */
public class FacesNavigation implements ServletContextAware {

    public static final String GLOBAL = "*";

    private String eventId;
    private ServletContext ctx;
    private Map<String, String> viewMap;

    public boolean setNavigateTo(String eventId) {
        this.eventId = eventId;
        return true;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        ctx = servletContext;
    }

    /**
     * Builds a mapping from outcome to view id for all navigation cases that apply globally.
     */
    private Map<String, String> buildViewMap(ApplicationAssociate appAssoc) {
        Map<String, String> result = new HashMap<>();
        for (ConfigNavigationCase navCase : appAssoc.getNavigationCaseListMappings().get(GLOBAL)) {
            String outcome = navCase.getFromOutcome();
            if (outcome != null) {
                result.put(outcome, navCase.getToViewId());
            }
        }
        return result;
    }

    /**
     * Lazy-loads a singleton map containing outcome-to-view mappings. It should only be called once JSF has started.
     */
    private Map<String, String> getViewMap() {
        if (viewMap == null) {
            viewMap = buildViewMap(ApplicationAssociate.getInstance(ctx));
        }
        return viewMap;
    }

    public String getNavigateTo() {
        Map<String, String> views = getViewMap();
        return views != null && views.containsKey(eventId) ? views.get(eventId) : eventId;
    }
}
