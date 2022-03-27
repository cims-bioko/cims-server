package com.github.cimsbioko.server.service.impl.indexing;

import com.github.cimsbioko.server.service.IndexingService;
import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.hibernate.search.jpa.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

public class IndexingServiceImpl implements IndexingService {

    private static final Logger log = LoggerFactory.getLogger(IndexingServiceImpl.class);

    private final EntityManager em;
    private final ApplicationEventPublisher eventPublisher;

    private final AtomicBoolean inProgress = new AtomicBoolean(false);
    private final AtomicReference<Status> status = new AtomicReference<>(new Status());

    public IndexingServiceImpl(EntityManager em, ApplicationEventPublisher eventPublisher) {
        this.em = em;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Status getStatus() {
        return status.get();
    }

    @EventListener
    @Order(10)
    public void handleIndexingStart(IndexingStartedEvent event) {
        log.info("indexing started");
        status.getAndUpdate(it -> Status.start());
    }

    @EventListener
    @Order(11)
    public void handleIndexingProgress(IndexingProgressEvent event) {
        log.info("indexed {}/{} documents ({}% at {} docs/s)",
                event.getDone(), event.getToDo(), event.getPercentComplete(), event.getSpeed());
        status.getAndUpdate(it ->
                it.updateProgress(event.getDone(), event.getToDo(), event.getElapsed(), event.getSpeed(), event.getPercentComplete())
        );
    }

    @EventListener
    @Order(10)
    public void handleIndexingComplete(IndexingCompletedEvent event) {
        log.info("indexing complete, indexed {} documents in {} minutes", event.getProcessed(), event.getDuration().toMinutes());
        inProgress.getAndSet(false);
        status.getAndUpdate(it -> it.updateComplete(event.getProcessed(), event.getDuration(), event.getCompletedAt()));
    }

    @Override
    @Transactional
    public void requestRebuild() {
        if (inProgress.compareAndSet(false, true)) {
            log.debug("search index rebuild requested, starting");
            eventPublisher.publishEvent(new IndexingStartedEvent(Instant.now()));
            Search.getFullTextEntityManager(em)
                    .createIndexer()
                    .progressMonitor(new IndexingEventPublisher(eventPublisher, 5000))
                    .start();
        } else {
            log.debug("search index rebuild requested, ignoring, already in progress");
        }
    }
}

class IndexingEventPublisher implements MassIndexerProgressMonitor {

    private final ApplicationEventPublisher eventPublisher;

    private final AtomicLong totalDone;
    private final LongAdder totalToDo;
    private volatile long startTime;
    private final int emitAfterNum;

    public IndexingEventPublisher(ApplicationEventPublisher eventPublisher, int emitAfterNum) {
        this.eventPublisher = eventPublisher;
        this.totalDone = new AtomicLong();
        this.totalToDo = new LongAdder();
        this.emitAfterNum = emitAfterNum;
    }

    public void entitiesLoaded(int size) {
    }

    public void documentsAdded(long increment) {
        long current = totalDone.addAndGet(increment);
        if (current == increment) {
            startTime = System.nanoTime();
        }
        if (current % (long) getStatusMessagePeriod() == 0L) {
            emitProgress(startTime, totalToDo.longValue(), current);
        }
    }

    public void documentsBuilt(int number) {
    }

    public void addToTotalCount(long count) {
        totalToDo.add(count);
        eventPublisher.publishEvent(new EntitiesAddedEvent(count, totalToDo.longValue()));
    }

    public void indexingCompleted() {
        eventPublisher.publishEvent(
                new IndexingCompletedEvent(Instant.now(), Duration.ofNanos(System.nanoTime() - startTime), totalDone.longValue())
        );
    }

    protected int getStatusMessagePeriod() {
        return emitAfterNum;
    }

    protected void emitProgress(long startTime, long totalToDoCount, long doneCount) {
        Duration elapsed = Duration.ofNanos(System.nanoTime() - startTime);
        float speed = (float) doneCount / (float) elapsed.getSeconds();
        float percentComplete = (float) doneCount * 100.0F / (float) totalToDoCount;
        eventPublisher.publishEvent(
                new IndexingProgressEvent(Instant.now(), doneCount, totalToDoCount, elapsed, speed, percentComplete)
        );
    }
}