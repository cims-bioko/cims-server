package com.github.cimsbioko.server.controller.service;

import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.domain.model.PrivilegeConstants;

public interface FieldWorkerService {

    @Authorized({PrivilegeConstants.VIEW_ENTITY})
    FieldWorker getUnknownFieldWorker();

}
