package com.github.cimsbioko.server.service.impl.campaign;

import com.github.cimsbioko.server.scripting.JsConfig;

public class CampaignUnloadedEvent implements CampaignEvent {

    private final String uuid;
    private final String name;
    private final JsConfig config;

    CampaignUnloadedEvent(String uuid, String name, JsConfig config) {
        this.uuid = uuid;
        this.name = name;
        this.config = config;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public JsConfig getConfig() {
        return config;
    }
}
