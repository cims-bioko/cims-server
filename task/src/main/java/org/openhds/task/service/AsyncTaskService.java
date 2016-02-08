package org.openhds.task.service;

import org.openhds.domain.model.AsyncTask;

import java.util.List;

public interface AsyncTaskService {

    String INDIVIDUAL_TASK_NAME = "Individual Task";
    String LOCATION_TASK_NAME = "Location Task";
    String RELATIONSHIP_TASK_NAME = "Relationship Task";
    String SOCIALGROUP_TASK_NAME = "Social Group Task";
    String VISIT_TASK_NAME = "Visit Task";
    String MEMBERSHIP_TASK_NAME = "Membership Task";
    String FIELDWORKER_TASK_NAME = "Field Worker Task";
    String LOCATIONHIERARCHY_TASK_NAME = "Location Hierarchy Task";

    boolean taskShouldRun(String taskName);

    void startTask(String taskName);

    void clearSession();

    void updateTaskProgress(String taskName, long itemsWritten);

    String getContentHash(String taskName);

    void finishTask(String taskName, long itemsWritten, String md5);

    List<AsyncTask> findAllAsyncTask();
}
