package com.github.cimsbioko.server.service.impl.indexing;

import java.time.Instant;

public class IndexingStartedEvent implements IndexingEvent {

    private final Instant started;

    public IndexingStartedEvent(Instant started) {
        this.started = started;
    }

    public Instant getStartTime() {
        return started;
    }
}
