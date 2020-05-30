package com.github.cimsbioko.server.service.impl.sync;

import com.github.cimsbioko.server.service.impl.MobileDbGeneratorEvent;

abstract class AbstractExportEvent implements MobileDbGeneratorEvent {

    private final String campaignUuid;

    AbstractExportEvent(String campaignUuid) {
        this.campaignUuid = campaignUuid;
    }

    @Override
    public String getCampaignUuid() {
        return campaignUuid;
    }
}
