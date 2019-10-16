package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.dao.TaskRepository;
import com.github.cimsbioko.server.domain.Task;
import com.github.cimsbioko.server.service.MobileDbGenerator;
import com.github.cimsbioko.server.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
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

    public SyncServiceImpl(TaskRepository repo, TaskScheduler scheduler, MobileDbGenerator generator) {
        this.repo = repo;
        this.scheduler = scheduler;
        this.generator = generator;
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void onCampaignUnload(CampaignUnloaded event) {
        log.info("unloading {}", event.getName());
        if (event.getName() == null) {
            cancelTask();
            schedule = null;
        }
    }

    @EventListener
    @Order
    public void onCampaignLoad(CampaignLoaded event) {
        log.info("loading {}", event.getName());
        if (event.getName() == null) {
            schedule = event.getConfig().getDatabaseExport().exportSchedule();
            resumeSchedule();
        }
    }

    public void resumeSchedule() {
        scheduleTask(schedule);
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onExportStarted(MobileDbGeneratorStarted e) {
        running = true;
        Task t = getTask();
        t.setStarted(Calendar.getInstance());
        t.setFinished(null);
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

    @Override
    public File getOutput() {
        return generator.getTarget();
    }

    @Override
    public Optional<String> getSchedule() {
        return Optional.ofNullable(schedule);
    }

    public void scheduleTask(String schedule) {

        cancelTask();

        this.schedule = Optional.ofNullable(schedule)
                .filter(s -> !s.trim().isEmpty())
                .orElse(null);

        if (this.schedule != null) {
            log.info("scheduling mobile db export task, schedule '{}'", this.schedule);
        } else {
            log.info("mobile db export task disabled by user settings");
        }

        scheduledTask = Optional.ofNullable(this.schedule)
                .map(CronTrigger::new)
                .map(t -> scheduler.schedule(this::requestTaskRun, t))
                .orElse(null);
    }

    public void cancelTask() {
        if (isTaskScheduled()) {
            log.info("canceling mobile db export task");
            scheduledTask.cancel(false);
        }
        scheduledTask = null;
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
    public void requestTaskRun() {
        if (!isTaskRunning()) {
            generator.generateMobileDb();
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
