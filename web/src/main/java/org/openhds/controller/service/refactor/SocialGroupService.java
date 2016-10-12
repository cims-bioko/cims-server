package org.openhds.controller.service.refactor;


import org.openhds.domain.annotations.Authorized;
import org.openhds.domain.model.SocialGroup;

import java.util.List;

public interface SocialGroupService extends EntityService<SocialGroup> {

    @Authorized("VIEW_ENTITY")
    List<SocialGroup> getAllSocialGroupsInRange(int i, int pageSize);

    @Authorized("VIEW_ENTITY")
    long getTotalSocialGroupCount();

}