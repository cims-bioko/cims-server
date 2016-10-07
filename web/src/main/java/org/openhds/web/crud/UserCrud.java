package org.openhds.web.crud;

import org.openhds.domain.model.User;

import javax.faces.model.SelectItem;
import java.util.List;

public interface UserCrud extends EntityCrud<User, String> {

    List<SelectItem> getRoleSelectItems();

    String getRetypedPassword();

    void setRetypedPassword(String retypedPassword);

    List<String> getRoles();

    void setRoles(List<String> roles);
}
