package com.github.cimsbioko.server.service.impl.campaign;

import java.io.File;

class CampaignUploadedEvent implements CampaignEvent {

    private final String uuid;
    private final File file;

    CampaignUploadedEvent(String uuid, File file) {
        this.uuid = uuid;
        this.file = file;
    }

    public String getUuid() {
        return uuid;
    }

    public File getFile() {
        return file;
    }
}
