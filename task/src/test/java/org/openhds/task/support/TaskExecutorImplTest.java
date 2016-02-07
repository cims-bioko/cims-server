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
    
//    @Test
//    public void shouldRunMembershipXmlTask() {
//        when(asyncTaskService.taskShouldRun(AsyncTaskService.MEMBERSHIP_TASK_NAME)).thenReturn(true);
//
//        executor.setMembershipTaskWriter(writeTask);
//        executor.executeMembershipTask();
//
//        verify(fileResolver).resolveMembershipFile();
//        verify(writeTask).writeXml(any(TaskContext.class));
//    }
//
//    @Test
//    public void shouldRunResidencyXmlTask() {
//        when(asyncTaskService.taskShouldRun(AsyncTaskService.RESIDENCY_TASK_NAME)).thenReturn(true);
//
//        executor.setResidencyTaskWriter(writeTask);
//        executor.executeResidencyXmlWriterTask();
//
//        verify(fileResolver).resolveResidencyXmlFile();
//        verify(writeTask).writeXml(any(TaskContext.class));
//    }
    
    @Test
    public void shouldRunIndividualXmlTask() {
        when(asyncTaskService.taskShouldRun(AsyncTaskService.INDIVIDUAL_TASK_NAME)).thenReturn(true);

        executor.setIndividualTaskWriter(writeTask);
        executor.executeIndividualTask();

        verify(fileResolver).resolveIndividualFile();
        verify(writeTask).run(any(TaskContext.class));
    }

    @Test
    public void shouldNotRunIndividualXmlTask() {
        when(asyncTaskService.taskShouldRun(AsyncTaskService.INDIVIDUAL_TASK_NAME)).thenReturn(false);

        executor.setIndividualTaskWriter(writeTask);
        executor.executeIndividualTask();

        verify(writeTask, times(0)).run(any(TaskContext.class));
    }

    @Test
    public void shouldRunLocationXmlTask() {
        when(asyncTaskService.taskShouldRun(AsyncTaskService.LOCATION_TASK_NAME)).thenReturn(true);

        executor.setLocationTaskWriter(writeTask);
        executor.executeLocationTask();

        verify(fileResolver).resolveLocationFile();
        verify(writeTask).run(any(TaskContext.class));
    }

    @Test
    public void shouldNotRunLocationXmlTask() {
        when(asyncTaskService.taskShouldRun(AsyncTaskService.LOCATION_TASK_NAME)).thenReturn(false);

        executor.setLocationTaskWriter(writeTask);
        executor.executeLocationTask();

        verify(writeTask, times(0)).run(any(TaskContext.class));
    }

    @Test
    public void shouldRunSocialGroupXmlTask() {
        when(asyncTaskService.taskShouldRun(AsyncTaskService.SOCIALGROUP_TASK_NAME)).thenReturn(true);

        executor.setSocialGroupTaskWriter(writeTask);
        executor.executeSocialGroupTask();

        verify(fileResolver).resolveSocialGroupFile();
        verify(writeTask).run(any(TaskContext.class));
    }

    @Test
    public void shouldNotRunSocialGroupXmlTask() {
        when(asyncTaskService.taskShouldRun(AsyncTaskService.SOCIALGROUP_TASK_NAME)).thenReturn(false);

        executor.setSocialGroupTaskWriter(writeTask);
        executor.executeSocialGroupTask();

        verify(writeTask, times(0)).run(any(TaskContext.class));
    }

    @Test
    public void shouldRunRelationshipXmlTask() {
        when(asyncTaskService.taskShouldRun(AsyncTaskService.RELATIONSHIP_TASK_NAME)).thenReturn(true);

        executor.setRelationshipTaskWriter(writeTask);
        executor.executeRelationshipTask();

        verify(fileResolver).resolveRelationshipFile();
        verify(writeTask).run(any(TaskContext.class));
    }

    @Test
    public void shouldNotRunRelationshipXmlTask() {
        when(asyncTaskService.taskShouldRun(AsyncTaskService.RELATIONSHIP_TASK_NAME)).thenReturn(false);

        executor.setRelationshipTaskWriter(writeTask);
        executor.executeRelationshipTask();

        verify(writeTask, times(0)).run(any(TaskContext.class));
    }
}
