package org.openhds.web.crud;

import org.openhds.domain.model.Role;

import javax.faces.model.SelectItem;
import java.util.List;

public interface RoleCrud extends EntityCrud<Role, String> {

    List<String> getPrivileges();

    void setPrivileges(List<String> privileges);

    List<SelectItem> getPrivilegeSelectItems();

}
