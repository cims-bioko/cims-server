package com.github.cimsbioko.server.task.support;

import com.github.cimsbioko.server.service.TaskService;
import com.github.cimsbioko.server.task.SyncFileTask;
import com.github.cimsbioko.server.task.TaskContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.github.cimsbioko.server.service.TaskService.MOBILEDB_TASK_NAME;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class TaskExecutorImplTest {

    @Mock
    private FileResolver fileResolver;

    @Mock
    private TaskService taskService;

    @Mock
    private SyncFileTask writeTask;

    private TaskExecutorImpl executor;

    @Before
    public void setUp() {
        initMocks(this);
        executor = new TaskExecutorImpl(taskService, fileResolver);
    }
    
    @Test
    public void shouldRunMobileDbTask() {

        when(taskService.taskShouldRun(MOBILEDB_TASK_NAME)).thenReturn(true);

        executor.setMobileDBWriter(writeTask);
        executor.executeMobileDBTask();

        verify(fileResolver).resolveMobileDBFile();
        verify(writeTask).run(any(TaskContext.class));
    }

    @Test
    public void shouldNotRunMobileDbTask() {

        when(taskService.taskShouldRun(MOBILEDB_TASK_NAME)).thenReturn(false);

        executor.setMobileDBWriter(writeTask);
        executor.executeMobileDBTask();

        verify(writeTask, times(0)).run(any(TaskContext.class));
    }
}
