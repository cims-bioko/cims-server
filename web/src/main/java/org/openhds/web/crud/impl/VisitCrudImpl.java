package org.openhds.web.crud.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.faces.context.FacesContext;

import org.openhds.controller.exception.AuthorizationException;
import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.domain.model.EntityType;
import org.openhds.domain.model.Visit;
import org.openhds.controller.service.VisitService;
import org.openhds.domain.service.SitePropertiesService;
import org.springframework.binding.message.MessageContext;

import static org.hibernate.Hibernate.initialize;

public class VisitCrudImpl extends EntityCrudImpl<Visit, String> {

	VisitService service;
    SitePropertiesService siteProperties;
	
	// used for manual conversion between Date and Calendar since the openFaces Calendar doesn't support JSF Converters
    Date visitDate;
	
	public VisitCrudImpl(Class<Visit> entityClass) {
        super(entityClass);
    }

	@Override
	protected void postSetup() {
		if (entityItem != null) {
			initialize(entityItem.getVisitLocation());
		}
	}

	/**
	 * Overridden method for setting extensionsInitialized paramater.
	 */
    public String createSetup() {
        reset(false, true);
		showListing = !isFlow;
        entityItem = newInstance();
        navMenuBean.setNextItem(entityClass.getSimpleName());
        navMenuBean.addCrumb(entityClass.getSimpleName() + " Create");
        return outcomePrefix + "_create";
    }
    
    // the entityitem, the pojo, can have its fields set before being created by super create
    // Note that super create calls dao.create(entityItem);
    
    @Override
    public String create() {

        try {
            service.createVisit(entityItem);
            return onCreateComplete();
        } catch (ConstraintViolations | AuthorizationException e) {
            jsfService.addError(e.getMessage());
        }
		return null;
    }
    
    @Override
    public String edit() {
    	
    	Visit persistedItem = (Visit)converter.getAsObject(FacesContext.getCurrentInstance(), null, jsfService.getReqParam("itemId"));

        try {
        	service.checkVisit(persistedItem, entityItem);
        	super.edit();
        	
        	return "pretty:visitEdit";
		} catch(Exception e) {
        	jsfService.addError(e.getMessage());
		}
        return null;
    }
    
    @Override
    public boolean commit(MessageContext messageContext) {
        try {
            service.createVisit(entityItem);
            return true;
        } catch (ConstraintViolations e) {
            webFlowService.createMessage(messageContext, e.getMessage());
        } 

        return false;
    }
     
    public Date getVisitDate() {
    	
    	if (entityItem.getVisitDate() == null)
    		return new Date();
    	
    	return entityItem.getVisitDate().getTime();
	}

	public void setVisitDate(Date visitDate) throws ParseException {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(visitDate);
		entityItem.setVisitDate(cal);
	}

	public VisitService getService() {
		return service;
	}

	public void setService(VisitService service) {
		this.service = service;
	}

	public SitePropertiesService getSiteProperties() {
		return siteProperties;
	}

	public void setSiteProperties(SitePropertiesService siteProperties) {
		this.siteProperties = siteProperties;
	}
}
