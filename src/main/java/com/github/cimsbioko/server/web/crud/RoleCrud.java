package com.github.cimsbioko.server.web.crud;

import com.github.cimsbioko.server.domain.Role;

import javax.faces.model.SelectItem;
import java.util.List;

public interface RoleCrud extends EntityCrud<Role, String> {

    List<String> getPrivileges();

    void setPrivileges(List<String> privileges);

    List<SelectItem> getPrivilegeSelectItems();

}
