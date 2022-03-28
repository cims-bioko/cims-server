package com.github.cimsbioko.server.search;

import com.github.cimsbioko.server.service.IndexingService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

public class InitialIndexer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(InitialIndexer.class);

    private final IndexingService indexingService;

    private final boolean reindexOnStartup;

    private final long delayInMinutes;

    private final TaskScheduler scheduler;

    public InitialIndexer(TaskScheduler scheduler, IndexingService indexingService, boolean reindexOnStartup, long delayInMinutes) {
        this.scheduler = scheduler;
        this.indexingService = indexingService;
        this.reindexOnStartup = reindexOnStartup;
        this.delayInMinutes = delayInMinutes;
    }

    @Override
    @Transactional
    public void onApplicationEvent(@NotNull final ApplicationReadyEvent event) {
        if (reindexOnStartup) {
            scheduleIndexing();
        } else {
            log.info("not reindexing db, disabled by user settings");
        }
    }

    private void scheduleIndexing() {
        Instant startTime = Instant.now().plus(Duration.ofMinutes(delayInMinutes));
        log.info("scheduling deferred indexing run for {}", startTime);
        scheduler.schedule(new IndexingTask(indexingService), startTime);
    }

}

class IndexingTask implements Runnable {

    private final IndexingService indexingService;

    public IndexingTask(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @Override
    public void run() {
        indexingService.requestRebuild();
    }
}