package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.Task;

import java.util.List;

public interface TaskService {

    String MOBILEDB_TASK_NAME = "Mobile DB Task";

    boolean taskShouldRun(String name);

    void startTask(String name);

    void updateTaskProgress(String name, long itemsWritten);

    String getDescriptor(String name);

    void finishTask(String name, long itemsWritten, String descriptorValue);

    List<Task> findAll();
}
