package com.github.cimsbioko.server.service.impl.indexing;

import java.time.Duration;
import java.time.Instant;

public class IndexingProgressEvent implements IndexingEvent {

    private final Instant time;
    private final long done;
    private final long toDo;
    private final Duration elapsed;
    private final float speed;
    private final float percentComplete;

    public IndexingProgressEvent(Instant time, long done, long toDo, Duration elapsed, float speed, float percentComplete) {
        this.time = time;
        this.done = done;
        this.toDo = toDo;
        this.elapsed = elapsed;
        this.speed = speed;
        this.percentComplete = percentComplete;
    }

    public Instant getTime() {
        return time;
    }

    public long getDone() {
        return done;
    }

    public long getToDo() {
        return toDo;
    }

    public Duration getElapsed() {
        return elapsed;
    }

    public float getSpeed() {
        return speed;
    }

    public float getPercentComplete() {
        return percentComplete;
    }
}
