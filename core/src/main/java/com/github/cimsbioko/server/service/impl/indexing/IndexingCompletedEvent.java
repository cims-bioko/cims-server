package com.github.cimsbioko.server.service.impl.indexing;

import java.time.Duration;
import java.time.Instant;

public class IndexingCompletedEvent implements IndexingEvent {

    private final Instant completedAt;
    private final Duration duration;
    private final long processed;

    public IndexingCompletedEvent(Instant completedAt, Duration indexingDuration, long processed) {
        this.completedAt = completedAt;
        this.duration = indexingDuration;
        this.processed = processed;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Duration getDuration() {
        return duration;
    }

    public long getProcessed() {
        return processed;
    }
}
