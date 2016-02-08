package org.openhds.task.service;

import org.openhds.domain.model.AsyncTask;

import java.util.List;

public interface AsyncTaskService {

    String MOBILEDB_TASK_NAME = "Mobile DB Task";

    boolean taskShouldRun(String taskName);

    void startTask(String taskName);

    void clearSession();

    void updateTaskProgress(String taskName, long itemsWritten);

    String getContentHash(String taskName);

    void finishTask(String taskName, long itemsWritten, String md5);

    List<AsyncTask> findAllAsyncTask();
}
