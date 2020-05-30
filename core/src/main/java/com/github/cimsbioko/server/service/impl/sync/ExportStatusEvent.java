package com.github.cimsbioko.server.service.impl.sync;

public class ExportStatusEvent extends AbstractExportEvent {

    private final int tablesProcessed;
    private final int percentDone;

    ExportStatusEvent(String campaignUuid) {
        this(campaignUuid, 0, 0);
    }

    ExportStatusEvent(String campaignUuid, int tablesProcessed, int percentDone) {
        super(campaignUuid);
        this.tablesProcessed = tablesProcessed;
        this.percentDone = percentDone;
    }

    public int getTablesProcessed() {
        return tablesProcessed;
    }

    public int getPercentDone() {
        return percentDone;
    }
}
