package org.openhds.web.beans;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhds.domain.model.AsyncTask;
import org.openhds.task.service.AsyncTaskService;
import org.openhds.task.support.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

public class TaskBean {

    private static final String TASK_VIEW = "task";
    private TaskExecutor taskExecutor;
    private AsyncTaskService asyncTaskService;
    private TaskScheduler scheduler;
    private ScheduledFuture scheduledTask;
    
    private Integer roundNumber;
    private String cronSchedule = "0 0 * * * ?";

    public TaskBean(TaskExecutor taskExecutor, AsyncTaskService asyncTaskService, TaskScheduler scheduler) {
        this.taskExecutor = taskExecutor;
        this.asyncTaskService = asyncTaskService;
        this.scheduler = scheduler;
    }

    public String startIndividualTask() {
        taskExecutor.executeIndividualXmlWriterTask();
        return TASK_VIEW;
    }

    public String startLocationTask() {
        taskExecutor.executeLocationXmlWriterTask();
        return TASK_VIEW;
    }

    public String startRelationshipTask() {
        taskExecutor.executeRelationshipXmlWriterTask();
        return TASK_VIEW;
    }

    public String startSocialGroupTask() {
        taskExecutor.executeSocialGroupXmlWriterTask();
        return TASK_VIEW;
    }
    
    public String startMembershipTask() {
    	taskExecutor.executeMembershipXmlWriterTask();
    	return TASK_VIEW;
    }
    
    public String startVisitTask() {
        if (roundNumber != null) {
            taskExecutor.executeVisitWriterTask(roundNumber);
        }
        return TASK_VIEW;
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
        scheduledTask = scheduler.schedule(new StartTasksExecutor(), new CronTrigger(cronSchedule));
        return TASK_VIEW;
    }

    private void startTasks() {
        startIndividualTask();
        startLocationTask();
        startRelationshipTask();
        startSocialGroupTask();
        startMembershipTask();
        startVisitTask();
        startLocationTask();
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
        return TASK_VIEW;
    }

    public List<AsyncTask> getTasks() {
        return asyncTaskService.findAllAsyncTask();
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getCronSchedule() {
        return cronSchedule;
    }

    public void setCronSchedule(String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }
}
