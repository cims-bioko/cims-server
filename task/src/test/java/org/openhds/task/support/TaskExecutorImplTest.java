package org.openhds.task.support;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhds.task.SyncFileTask;
import org.openhds.task.TaskContext;
import org.openhds.task.service.AsyncTaskService;

import static org.openhds.task.service.AsyncTaskService.MOBILEDB_TASK_NAME;

public class TaskExecutorImplTest {

    @Mock
    FileResolver fileResolver;

    @Mock
    AsyncTaskService asyncTaskService;

    @Mock
    private SyncFileTask writeTask;

    private TaskExecutorImpl executor;

    @Before
    public void setUp() {
        initMocks(this);
        executor = new TaskExecutorImpl(asyncTaskService, fileResolver);
    }
    
    @Test
    public void shouldRunIndividualXmlTask() {
        when(asyncTaskService.taskShouldRun(MOBILEDB_TASK_NAME)).thenReturn(true);

        executor.setMobileDBWriter(writeTask);
        executor.executeMobileDBTask();

        verify(fileResolver).resolveMobileDBFile();
        verify(writeTask).run(any(TaskContext.class));
    }

    @Test
    public void shouldNotRunIndividualXmlTask() {
        when(asyncTaskService.taskShouldRun(MOBILEDB_TASK_NAME)).thenReturn(false);

        executor.setMobileDBWriter(writeTask);
        executor.executeMobileDBTask();

        verify(writeTask, times(0)).run(any(TaskContext.class));
    }
}
