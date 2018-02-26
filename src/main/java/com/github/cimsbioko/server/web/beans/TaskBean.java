package com.github.cimsbioko.server.web.beans;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.github.cimsbioko.server.service.CurrentUser;
import com.github.cimsbioko.server.domain.Task;
import com.github.cimsbioko.server.service.TaskService;
import com.github.cimsbioko.server.task.support.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

public class TaskBean {

    private static final Logger log = LoggerFactory.getLogger(TaskBean.class);

    private static final String TASK_VIEW = "task";
    private TaskExecutor taskExecutor;
    private TaskService taskService;
    private TaskScheduler scheduler;
    private CurrentUser currentUser;

    private ScheduledFuture scheduledTask;

    @Value("${app.task.schedule}")
    private String cronSchedule;

    public TaskBean(TaskExecutor taskExecutor, TaskService taskService, TaskScheduler scheduler,
                    CurrentUser currentUser) {
        this.taskExecutor = taskExecutor;
        this.taskService = taskService;
        this.scheduler = scheduler;
        this.currentUser = currentUser;
    }

    public String startMobileDBTask() {
        taskExecutor.executeMobileDBTask();
        return TASK_VIEW;
    }

    private void startTasks() {
        startMobileDBTask();
    }

    public String startAllTasks() {
        startTasks();
        return TASK_VIEW;
    }

    class StartTasksExecutor implements Runnable {
        @Override
        public void run() {
            TaskBean.this.startTasks();
        }
    }

    public String scheduleAllTasks() {
        cancelScheduled();
        if (cronSchedule != null && !cronSchedule.isEmpty()) {
            scheduledTask = scheduler.schedule(new StartTasksExecutor(), new CronTrigger(cronSchedule));
            log.info("user {} scheduled tasks for schedule {}", currentUser, cronSchedule);
        } else {
            log.info("user {} disabled scheduled tasks", currentUser, cronSchedule);
        }
        return TASK_VIEW;
    }

    public String getNextScheduledRun() {
        if (scheduledTask == null) {
            return "None";
        }
        return "In " + scheduledTask.getDelay(TimeUnit.MINUTES) + " min";
    }

    private void cancelScheduled() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
    }

    public String cancelScheduledTasks() {
        cancelScheduled();
        log.info("user {} disabled scheduled tasks", currentUser);
        return TASK_VIEW;
    }

    public List<Task> getTasks() {
        return taskService.findAll();
    }

    public String getCronSchedule() {
        return cronSchedule;
    }

    public void setCronSchedule(String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }
}
