package com.github.cimsbioko.server.task.support;

public class TaskExecutorImplTest {

//    @Mock
//    private FileResolver fileResolver;
//
//    @Mock
//    private TaskService taskService;
//
//    @Mock
//    private SyncFileTask writeTask;
//
//    private TaskExecutorImpl executor;
//
//    @Before
//    public void setUp() {
//        initMocks(this);
//        executor = new TaskExecutorImpl(taskService, fileResolver);
//    }
//
//    @Test
//    public void shouldRunMobileDbTask() {
//
//        when(taskService.taskShouldRun(MOBILEDB_TASK_NAME)).thenReturn(true);
//
//        executor.setMobileDBWriter(writeTask);
//        executor.executeMobileDBTask();
//
//        verify(fileResolver).resolveMobileDBFile();
//        verify(writeTask).run(any(TaskContext.class));
//    }
//
//    @Test
//    public void shouldNotRunMobileDbTask() {
//
//        when(taskService.taskShouldRun(MOBILEDB_TASK_NAME)).thenReturn(false);
//
//        executor.setMobileDBWriter(writeTask);
//        executor.executeMobileDBTask();
//
//        verify(writeTask, times(0)).run(any(TaskContext.class));
//    }
}
