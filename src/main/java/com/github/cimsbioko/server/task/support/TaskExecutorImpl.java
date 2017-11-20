package com.github.cimsbioko.server.task.support;

import com.github.cimsbioko.server.task.SyncFileTask;
import com.github.cimsbioko.server.task.TaskContext;
import com.github.cimsbioko.server.task.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

import javax.annotation.Resource;

@Component("openhdsTaskExecutor")
public class TaskExecutorImpl implements TaskExecutor {

    private FileResolver fileResolver;
    private TaskService taskService;
    private SyncFileTask mobileDBWriter;

    @Autowired
    public TaskExecutorImpl(TaskService taskService, FileResolver fileResolver) {
        this.taskService = taskService;
        this.fileResolver = fileResolver;
    }

    @Override
    public void executeMobileDBTask() {
        if (taskService.taskShouldRun(TaskService.MOBILEDB_TASK_NAME)) {
            taskService.startTask(TaskService.MOBILEDB_TASK_NAME);
            File dbFile = fileResolver.resolveMobileDBFile();
            mobileDBWriter.run(new TaskContext(dbFile));
        }
    }

    @Resource(name = "mobileDBWriter")
    public void setMobileDBWriter(SyncFileTask mobileDBWriter) {
        this.mobileDBWriter = mobileDBWriter;
    }
}
