package com.github.cimsbioko.server.controller.service;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.PrivilegeConstants;
import com.github.cimsbioko.server.domain.model.SocialGroup;

public interface SocialGroupService {

    @Authorized({PrivilegeConstants.CREATE_ENTITY})
    SocialGroup generateId(SocialGroup entityItem) throws ConstraintViolations;

}
