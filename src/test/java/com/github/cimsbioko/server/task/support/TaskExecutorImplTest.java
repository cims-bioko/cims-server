package com.github.cimsbioko.server.task.support;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.github.cimsbioko.server.task.TaskContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import com.github.cimsbioko.server.task.SyncFileTask;
import com.github.cimsbioko.server.task.service.TaskService;

import static com.github.cimsbioko.server.task.service.TaskService.MOBILEDB_TASK_NAME;

public class TaskExecutorImplTest {

    @Mock
    FileResolver fileResolver;

    @Mock
    TaskService taskService;

    @Mock
    private SyncFileTask writeTask;

    private TaskExecutorImpl executor;

    @Before
    public void setUp() {
        initMocks(this);
        executor = new TaskExecutorImpl(taskService, fileResolver);
    }
    
    @Test
    public void shouldRunIndividualXmlTask() {
        when(taskService.taskShouldRun(MOBILEDB_TASK_NAME)).thenReturn(true);

        executor.setMobileDBWriter(writeTask);
        executor.executeMobileDBTask();

        verify(fileResolver).resolveMobileDBFile();
        verify(writeTask).run(any(TaskContext.class));
    }

    @Test
    public void shouldNotRunIndividualXmlTask() {
        when(taskService.taskShouldRun(MOBILEDB_TASK_NAME)).thenReturn(false);

        executor.setMobileDBWriter(writeTask);
        executor.executeMobileDBTask();

        verify(writeTask, times(0)).run(any(TaskContext.class));
    }
}
