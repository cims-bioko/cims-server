package com.github.cimsbioko.server.task.service;

import com.github.cimsbioko.server.domain.model.AsyncTask;

import java.util.List;

public interface AsyncTaskService {

    String MOBILEDB_TASK_NAME = "Mobile DB Task";

    boolean taskShouldRun(String name);

    void startTask(String name);

    void clearSession();

    void updateTaskProgress(String name, long itemsWritten);

    String getDescriptor(String name);

    void finishTask(String name, long itemsWritten, String descriptorValue);

    List<AsyncTask> findAll();
}
