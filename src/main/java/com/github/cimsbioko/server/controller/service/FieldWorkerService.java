package com.github.cimsbioko.server.controller.service;

import com.github.cimsbioko.server.domain.model.FieldWorker;
import org.springframework.security.access.prepost.PreAuthorize;

public interface FieldWorkerService {

    @PreAuthorize("hasAuthority('VIEW_ENTITY')")
    FieldWorker getUnknownFieldWorker();

}
