package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.service.SitePropertiesService;

public class SitePropertiesServiceImpl implements SitePropertiesService {

    private String unknownIdentifier;

    public String getUnknownIdentifier() {
        return unknownIdentifier;
    }

    public void setUnknownIdentifier(String unknownIdentifier) {
        this.unknownIdentifier = unknownIdentifier;
    }

}
