package org.openhds.web.crud.impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;


import org.openhds.controller.exception.AuthorizationException;
import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.EntityService;
import org.openhds.dao.service.Dao;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.constraint.Searchable;
import org.openhds.domain.model.AuditableCollectedEntity;
import org.openhds.web.crud.EntityCrud;
import org.openhds.web.service.JsfService;
import org.openhds.web.ui.NavigationMenuBean;
import org.openhds.web.ui.PagingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic implementation of the EntityCrud interface
 * This class is currently being used by all entities in the system
 * It is being wired up in the applicationContext.xml file
 *
 * @param <T>
 * @param <PK>
 */

public class EntityCrudImpl<T, PK extends Serializable> implements EntityCrud<T, PK> {

    static Logger log = LoggerFactory.getLogger(EntityCrudImpl.class);
    
    // This is currently being used to create a new
    // instance of the entity type
    protected Class<T> entityClass;
    
    String outcomePrefix;
    
    
    // The current entity (or "backing bean") being worked on
    // wired from the applicationContext.xml file
    protected T entityItem;
   
    PagingState pager;
    PagingState filteredPager;
    protected Dao<T, PK> dao;
    GenericDao genericDao;
    
	List<T> pagedItems;
    HashMap<T, Class<?>> searchableFieldsMap = new HashMap<>();
    NavigationMenuBean navMenuBean;
    
	String propertyName;
    String searchString;
    List<SelectItem> searchableFieldsList;
    Boolean isSearch = false;

	// used to convert an entity from a string to object, or
    // object to a string
    protected Converter converter;
   
    // helper service
    public JsfService jsfService;
   
	protected EntityFilter<T> entityFilter;
	
	protected boolean showListing = true;
	
	interface EntityFilter<T> {
		List<T> getFilteredEntityList(T entityItem);
	}

	/**
	 * Service that provides the business logic for creating, editing and deleting entities
	 */
	protected EntityService entityService;

    public EntityCrudImpl(Class<T> entityClass) {
        if (entityClass == null) {
            throw new IllegalArgumentException("entity class type required for crud");
        }
        this.entityClass = entityClass;
        this.outcomePrefix = entityClass.getSimpleName().toLowerCase();
        pager = new PagingState();
        filteredPager = new PagingState();
    }

	public void setJsfService(JsfService jsfService) {
        this.jsfService = jsfService;
    }

    public void setDao(Dao<T, PK> dao) {
        this.dao = dao;
    }

    public Dao<T, PK> getDao() {
        return dao;
    }
    
    public PagingState getPager() {
        if (pager.getTotalCount() < 0) {
        	if (isSearch) {
            	pager.setTotalCount(processSearchCriteria());
        	} else {
        		pager.setTotalCount(dao.getTotalCount());
        	}
        }
        return pager;
    }
    
    public PagingState getFilteredPager() {
        if (filteredPager.getTotalCount() < 0) {
        	if (isSearch) {
        		filteredPager.setTotalCount(processSearchCriteria());
        	} else {
        		filteredPager.setTotalCount(getFilteredPagedItems().size());
        	}
        }
        return filteredPager;
    }

    /**
     * Create a new instance of the entity type
     * @return the new object instance
     */
    protected T newInstance() {
        try {
            return entityClass.newInstance();
        } catch (Exception e) {
            throw new FacesException("failed to instantiate entity "
                    + entityClass.getName(), e);
        }
    }

    /**
     *
     * @param resetPaging
     */
    protected void reset(boolean resetPaging, boolean resetEntityitem) {
    	if (resetEntityitem) {
    		entityItem = null;
    	}
        pager.setTotalCount(-1);
        pagedItems = null;
        if (resetPaging) {
            pager.setPageIndex(0);
        }
    }

    public String listSetup() {
        reset(true, true);        
        return outcomePrefix + "_list";
        
    }

    public void validateCreate(FacesContext facesContext,
            UIComponent component, Object value) {
        T newItem = null;
        try {
            newItem = entityClass.newInstance();
        } catch (Exception e) {
            log.error("failed to create item instance", e);
        }
        String newItemString = converter.getAsString(FacesContext.getCurrentInstance(), null, newItem);
        String itemString = converter.getAsString(FacesContext.getCurrentInstance(), null, entityItem);
        if (!newItemString.equals(itemString)) {
            createSetup();
        }
    }

    public String createSetup() {
        reset(false, true);
        showListing = true;
        entityItem = newInstance();
        navMenuBean.setNextItem(entityClass.getSimpleName());
        navMenuBean.addCrumb(entityClass.getSimpleName() + " Create");
        return outcomePrefix + "_create";
    }

    /**
     * Persist the current entity item to the database
     */
    public String create() {
    	try {
			entityService.create(entityItem);
		} catch (IllegalArgumentException e) {
			jsfService.addError(e.getMessage());
			return null;
		} catch (ConstraintViolations e) {
			for(String msg : e.getViolations()) {
				jsfService.addError(msg);
			}
			return null;
		} catch (SQLException e) {
			jsfService.addError("Error creating record in database");
			return null;
		} catch(AuthorizationException e) {
			jsfService.addError(e.getMessage());
			return null;
		} catch(Exception e) {
			jsfService.addError(e.getMessage());
			return null;
		}
    	

		return onCreateComplete();
    }

    protected String onCreateComplete() {
        showListing = true;
        return listSetup();
    }

    public String detailSetup() {
    	showListing = false;
        navMenuBean.setNextItem(entityClass.getSimpleName());
    	navMenuBean.addCrumb(entityClass.getSimpleName() + " Detail");
        return scalarSetup(outcomePrefix + "_detail");
    }

    public String editSetup() {
    	showListing = false;
        navMenuBean.setNextItem(entityClass.getSimpleName());
    	navMenuBean.addCrumb(entityClass.getSimpleName() + " Edit");
    	String result = scalarSetup(outcomePrefix + "_edit");
    	if (AuditableCollectedEntity.class.isAssignableFrom(entityClass)) {
    	    // load field worker to avoid lazy load exeptions
    	    ((AuditableCollectedEntity)entityItem).getCollectedBy().getExtId();
    	}
    	return result;
    }

    /**
     * Update a persisted entity
     */
    public String edit() {
    	// verify that the entity being edited is valid
    	// there is a case when the user could open the edit form
    	// by manually navigating to the URL
    	String itemString = converter.getAsString(FacesContext.getCurrentInstance(), null, entityItem);
        String itemId = jsfService.getReqParam("itemId");
        
        if (itemString == null || itemString.length() == 0
                || !itemString.equals(itemId)) {
            String outcome = editSetup();
            if ((outcomePrefix + "_edit").equals(outcome)) {
                jsfService.addError("Could not edit item. Try again.");
            }
            return outcome;
        }

        // attempt to update
        try {
			entityService.save(entityItem);
        } catch(ConstraintViolations e) {
			for(String msg : e.getViolations()) {
				jsfService.addError(msg);
			}
			return null;
		} catch (SQLException e) {
			jsfService.addError("Error updating the current entity");
			return null;
		} catch (AuthorizationException e) {
			jsfService.addError(e.getMessage());
			return null;
		} catch (Exception e) {
			jsfService.addError("Error updating entity");
			return null;
		}

		showListing = true;
        return detailSetup();
    }

    /**
     * Remove/delete an entity from persistence
     */
     public String delete() {

        Object persistentObject = converter.getAsObject(FacesContext.getCurrentInstance(), null, jsfService.getReqParam("itemId"));
        
        try {
			entityService.delete(persistentObject);
		} catch (SQLException e) {
			jsfService.addError("Could not delete the persistent entity");
			return null;
		} catch (AuthorizationException e) {
			jsfService.addError(e.getMessage());
			return null;
		}

		return onCreateComplete();
    }

    @SuppressWarnings("unchecked")
    protected String scalarSetup(String outcome) {
        reset(false, true);
        entityItem = (T) jsfService.getObjViaReqParam("itemId", converter, null);
        if (entityItem == null) {
            String itemId = jsfService.getReqParam("itemId");
            jsfService.addError("The item with id " + itemId
                    + " no longer exists.");
        }
        postSetup();
        return outcome;
    }

    /**
     * Allows derived classes to perform post-processing after scalarSetup, but before
     * dispatch to result.
     */
    protected void postSetup() { }

    public String next() {
        reset(false, false);
        getPager().nextPage();
        return outcomePrefix + "_list";
    }

    public String prev() {
        reset(false, false);
        getPager().previousPage();
        return outcomePrefix + "_list";
    }

    public String lastPage() {
    	reset(false, false);
    	// set pager to the total number of records
    	// it will automatically adjust and show the last page
    	getPager().setPageIndex((int)getPager().getTotalCount());
    	return outcomePrefix + "_list";
    }
    
    public String firstPage() {
    	reset(false, false);
    	getPager().setPageIndex(0);
    	return outcomePrefix + "_list";
    }
    
    public T getItem() {
        if (entityItem == null) {
            entityItem = newInstance();
        }
        return entityItem;
    }

    public List<T> getPagedItems() {

    	// normal paging of data records
        if (pagedItems == null)  
        	pagedItems = dao.findPaged(pager.getPageIncrement(), pager.getPageIndex());           
        
        // user has performed a search, so only grab a subset of those records
        else if (isSearch) 
        	pagedItems = generateSearchResults();       

    	return pagedItems;
    }

    public String search() {
    	
    	// clear out any entity the user might be editing and reset the pager
    	reset(true, false);
    	
    	// make sure user entered a valid string to search
    	if(searchString != null && searchString.trim().length() != 0){
        	isSearch = true;
    	} else {
         	isSearch = false;
         	return outcomePrefix + "_list";
    	}
    	getPager().setTotalCount(processSearchCriteria());
    	    	
        return outcomePrefix + "_list";
    }
    
    private long processSearchCriteria() {
    	if (!"java.lang.String".equals(searchableFieldsMap.get(propertyName).getName()))
    		 return searchForEntitiesById(propertyName, searchableFieldsMap.get(propertyName), searchString, entityClass).size();
    	else 
	    	return dao.getCountByProperty(propertyName, searchString);
    }
    
    private List<T> generateSearchResults() {
    	if (!"java.lang.String".equals(searchableFieldsMap.get(propertyName).getName()))
    		 return searchForEntitiesById(propertyName, searchableFieldsMap.get(propertyName), searchString, entityClass);

	    return dao.findListByPropertyPagedInsensitive(propertyName, searchString, pager.getPageIndex(), pager.getPageIncrement());
    }
    
	public String clearSearch() {
		isSearch = false;
		searchString = null;
		propertyName = null;
		reset(true, false);
		return outcomePrefix + "_list";
	}
	
	@SuppressWarnings("unchecked")
	public List<T> searchForEntitiesById(String searchPropertyName, Class<?> propertyName, String propertyValue, Class<T> entityClass) {
		T item = (T) genericDao.findByProperty(propertyName, "extId", propertyValue, true);
		List<T> list = genericDao.findListByProperty(entityClass, searchPropertyName, item, true);
		return list;
	}

    public SelectItem[] getSelectItems() {
        return jsfService.getSelectItems(dao.findAll(true));
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public Converter getConverter() {
        return converter;
    }

    public void setItem(T entityItem) {
        this.entityItem = entityItem;
    }

    public void performAudit(T entityItem) {
    }

	public List<T> getFilteredPagedItems() {
		if (entityFilter == null) {
			// filter not setup so can't filter entities
			// fall back to normal pagedItems
			return getPagedItems();
		}
		
		return entityFilter.getFilteredEntityList(entityItem);
		//return getPagedItems();
	}
	
	public EntityService getEntityService() {
		return entityService;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	/*
	 * Used in EL expressions for search box.
	 */
	@SuppressWarnings("unchecked")
	public List<SelectItem> getSearchableFieldsList() {
		searchableFieldsList = new ArrayList<>();
		Field[] f = entityClass.getDeclaredFields();

		for (Field ff : f) {	
				if (ff.isAnnotationPresent(Searchable.class)) {
					searchableFieldsMap.put((T) ff.getName(), ff.getType());
					SelectItem it = new SelectItem(ff.getName());
					searchableFieldsList.add(it);
				}
		}    	
		return searchableFieldsList;
	}

	public GenericDao getGenericDao() {
		return genericDao;
	}

	public void setGenericDao(GenericDao genericDao) {
		this.genericDao = genericDao;
	}

	/*
	 * Used in EL expressions for expanders in menu.
	 */
	public boolean isShowListing() {
		return showListing;
	}

	/*
	 * Used in EL expressions to toggle expanders in menu.
	 */
	public void setShowListing(boolean showListing) {
		this.showListing = showListing;
	}

	public void setNavMenuBean(NavigationMenuBean navMenuBean) {
		this.navMenuBean = navMenuBean;
	}
}
