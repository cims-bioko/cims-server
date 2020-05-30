package com.github.cimsbioko.server.service.impl.sync;

class ExportStartedEvent extends AbstractExportEvent {
    ExportStartedEvent(String campaignUuid) {
        super(campaignUuid);
    }
}
