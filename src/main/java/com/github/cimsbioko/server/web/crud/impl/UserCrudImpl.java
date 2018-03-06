package com.github.cimsbioko.server.web.crud.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.faces.model.SelectItem;

import com.github.cimsbioko.server.domain.Role;
import com.github.cimsbioko.server.domain.User;
import com.github.cimsbioko.server.web.crud.UserCrud;
import com.github.cimsbioko.server.service.UserService;

import static org.hibernate.Hibernate.initialize;

public class UserCrudImpl extends EntityCrudImpl<User, String> implements UserCrud {

    UserService service;
    String retypedPassword;
    List<String> roles;

    public UserCrudImpl(Class<User> entityClass) {
        super(entityClass);
        roles = new ArrayList<>();
    }

    @Override
    protected void postSetup() {
        if (entityItem != null) {
            initialize(entityItem.getRoles());
        }
    }

    @Override
    public String create() {

        try {
            service.evaluateUser(entityItem, retypedPassword);
            service.convertAndSetRoles(entityItem, roles);
            roles = null;
            super.create();
        } catch (Exception e) {
            jsfService.addError(e.getMessage());
        }
        return null;
    }

    @Override
    public String edit() {

        try {
            entityItem.getRoles().clear();
            service.checkUser(entityItem, retypedPassword);
            service.convertAndSetRoles(entityItem, roles);
            entityService.save(entityItem);
        } catch (Exception e) {
            jsfService.addError(e.getMessage());
        }

        return detailSetup();
    }

    /**
     * Retrieves the available roles to be displayed as checkboxes on the UI.
     */
    @Override
    public List<SelectItem> getRoleSelectItems() {
        List<SelectItem> rolesSelectItems = new ArrayList<>();
        List<Role> roles = service.getRoles();

        for (Role role : roles) {
            if (role.getDeleted() == null)
                rolesSelectItems.add(new SelectItem(role.getName()));
        }
        return rolesSelectItems;
    }

    @Override
    public String getRetypedPassword() {
        return retypedPassword;
    }

    @Override
    public void setRetypedPassword(String retypedPassword) {
        this.retypedPassword = retypedPassword;
    }

    public UserService getService() {
        return service;
    }

    public void setService(UserService service) {
        this.service = service;
    }

    /**
     * Get all roles of which the entityItem belongs to.
     */
    @Override
    public List<String> getRoles() {
        Set<Role> roles = entityItem.getRoles();
        List<String> list = new ArrayList<>();
        for (Role r : roles) {
            list.add(r.getName());
        }
        return list;
    }

    @Override
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

}
