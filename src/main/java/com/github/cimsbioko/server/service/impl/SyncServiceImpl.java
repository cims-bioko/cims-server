package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.dao.TaskRepository;
import com.github.cimsbioko.server.domain.Task;
import com.github.cimsbioko.server.service.MobileDbGenerator;
import com.github.cimsbioko.server.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MINUTES;

public class SyncServiceImpl implements SyncService {

    private static final String TASK_NAME = "Mobile DB Task";

    private static final Logger log = LoggerFactory.getLogger(SyncServiceImpl.class);

    private String schedule;

    private TaskScheduler scheduler;

    private TaskRepository repo;

    private MobileDbGenerator generator;

    private ScheduledFuture scheduledTask;

    private boolean running;

    public SyncServiceImpl(TaskRepository repo, TaskScheduler scheduler, MobileDbGenerator generator, String schedule) {
        this.repo = repo;
        this.scheduler = scheduler;
        this.generator = generator;
        this.schedule = schedule;
    }

    @PostConstruct
    void initialStartup() {
        scheduleTask(schedule);
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onExportStarted(MobileDbGeneratorStarted e) {
        running = true;
        getTask().setStarted(Calendar.getInstance());
    }


    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onExportUpdate(MobileDbGeneratorUpdate e) {
        getTask().setItemCount(e.getTablesProcessed());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onExportFinished(MobileDbGeneratorFinished e) {
        running = false;
        Task t = getTask();
        t.setFinished(Calendar.getInstance());
        t.setDescriptor(e.getContentHash());
    }

    public void scheduleTask(String schedule) {
        if (isTaskScheduled()) {
            log.info("cancelling db export task");
            cancelTask();
        }
        log.info("scheduling mobile db export task, schedule {}", schedule);
        scheduledTask = Optional.ofNullable(schedule)
                .filter(s -> !s.trim().isEmpty())
                .map(CronTrigger::new)
                .map(t -> scheduler.schedule(this::requestTaskRun, t))
                .orElse(null);
    }

    public boolean cancelTask() {
        log.info("canceling mobile database export task");
        if (isTaskScheduled()) {
            scheduledTask.cancel(false);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isTaskScheduled() {
        return Optional.ofNullable(scheduledTask)
                .map(f -> !f.isDone())
                .orElse(false);
    }

    @Override
    public boolean isTaskRunning() {
        return running;
    }

    @Override
    public boolean requestTaskRun() {
        if (isTaskRunning()) {
            return false;
        } else {
            generator.generateMobileDb();
            return true;
        }
    }

    public Optional<Long> getMinutesToNextRun() {
        return Optional.ofNullable(scheduledTask)
                .map(t -> t.getDelay(MINUTES));
    }

    @Transactional(readOnly = true)
    public Task getTask() {
        return repo.findByName(TASK_NAME);
    }
}
