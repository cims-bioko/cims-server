package com.github.cimsbioko.server.controller.service.refactor;


import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.SocialGroup;

import java.util.List;

public interface SocialGroupService extends EntityService<SocialGroup> {

    @Authorized("VIEW_ENTITY")
    List<SocialGroup> getAllSocialGroupsInRange(int i, int pageSize);

    @Authorized("VIEW_ENTITY")
    long getTotalSocialGroupCount();

}