package com.github.cimsbioko.server.service;


import java.time.Duration;
import java.time.Instant;

public interface IndexingService {

    class Status {

        private final boolean running;
        private final long processed;
        private final long toDo;
        private final Duration elapsed;
        private final float percentComplete;
        private final float docsPerSecond;
        private final Instant completedAt;

        public Status() {
            this(false, 0, 0, Duration.ZERO, 0, 0, null);
        }

        private Status(boolean running, long processed, long toDo, Duration elapsed, float percentComplete, float docsPerSecond, Instant completedAt) {
            this.running = running;
            this.processed = processed;
            this.toDo = toDo;
            this.elapsed = elapsed;
            this.percentComplete = percentComplete;
            this.docsPerSecond = docsPerSecond;
            this.completedAt = completedAt;
        }

        public Status updateComplete(long processed, Duration elapsed, Instant completedAt) {
            return new Status(false, processed, processed, elapsed, percentComplete, docsPerSecond, completedAt);
        }

        public Status updateProgress(long done, long toDo, Duration elapsed, float docsPerSecond, float percentComplete) {
            return new Status(running, done, toDo, elapsed, percentComplete, docsPerSecond, completedAt);
        }

        public static Status start() {
            return new Status(true, 0, 0, Duration.ZERO, 0, 0, null);
        }

        public boolean isRunning() {
            return running;
        }

        public long getProcessed() {
            return processed;
        }

        public long getToDo() {
            return toDo;
        }

        public Duration getElapsed() {
            return elapsed;
        }

        public float getPercentComplete() {
            return percentComplete;
        }

        public float getDocsPerSecond() {
            return docsPerSecond;
        }

        public Instant getCompletedAt() {
            return completedAt;
        }
    }

    Status getStatus();

    void requestRebuild();

}
