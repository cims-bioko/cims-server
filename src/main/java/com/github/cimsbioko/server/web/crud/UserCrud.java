package com.github.cimsbioko.server.web.crud;

import com.github.cimsbioko.server.domain.model.User;

import javax.faces.model.SelectItem;
import java.util.List;

public interface UserCrud extends EntityCrud<User, String> {

    List<SelectItem> getRoleSelectItems();

    String getRetypedPassword();

    void setRetypedPassword(String retypedPassword);

    List<String> getRoles();

    void setRoles(List<String> roles);
}
