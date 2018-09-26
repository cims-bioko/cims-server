package com.github.cimsbioko.server.task.support;

import com.github.cimsbioko.server.task.SyncFileTask;
import com.github.cimsbioko.server.task.TaskContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

@Component("openhdsTaskExecutor")
public class TaskExecutorImpl implements TaskExecutor {

    private FileResolver fileResolver;
    private SyncFileTask mobileDBWriter;

    @Autowired
    public TaskExecutorImpl(FileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

    @Override
    public void executeMobileDBTask() {
        File dbFile = fileResolver.resolveMobileDBFile();
        mobileDBWriter.run(new TaskContext(dbFile));
    }

    @Resource(name = "mobileDBWriter")
    public void setMobileDBWriter(SyncFileTask mobileDBWriter) {
        this.mobileDBWriter = mobileDBWriter;
    }
}
