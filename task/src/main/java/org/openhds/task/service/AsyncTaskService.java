package org.openhds.task.service;

import java.util.List;

import org.openhds.domain.model.AsyncTask;

public interface AsyncTaskService {

    String INDIVIDUAL_TASK_NAME = "Individual Task";
    String LOCATION_TASK_NAME = "Location Task";
    String RELATIONSHIP_TASK_NAME = "Relationship Task";
    String SOCIALGROUP_TASK_NAME = "Social Group Task";
    String VISIT_TASK_NAME = "Visit Task";
    String RESIDENCY_TASK_NAME = "Residency Task";
    String MEMBERSHIP_TASK_NAME = "Membership Task";

    boolean taskShouldRun(String taskName);

    void startTask(String taskName);

    void clearSession();

    void updateTaskProgress(String taskName, long itemsWritten);

    String getContentHash(String taskName);

    void finishTask(String taskName, long itemsWritten, String md5);

    List<AsyncTask> findAllAsyncTask();
}
