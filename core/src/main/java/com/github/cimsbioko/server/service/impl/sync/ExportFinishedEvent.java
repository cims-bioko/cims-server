package com.github.cimsbioko.server.service.impl.sync;

public class ExportFinishedEvent extends AbstractExportEvent {

    private final String contentHash;

    ExportFinishedEvent(String campaignUuid) {
        this(campaignUuid, "generation failed - no content");
    }

    ExportFinishedEvent(String campaignUuid, String contentHash) {
        super(campaignUuid);
        this.contentHash = contentHash;
    }

    public String getContentHash() {
        return contentHash;
    }
}
