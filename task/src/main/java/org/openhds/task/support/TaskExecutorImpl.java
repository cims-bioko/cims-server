package org.openhds.task.support;

import org.openhds.task.TaskContext;
import org.openhds.task.SyncFileTask;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

@Component("openhdsTaskExecutor")
public class TaskExecutorImpl implements TaskExecutor {

    private FileResolver fileResolver;
    private AsyncTaskService asyncTaskService;

    private SyncFileTask individualTaskWriter;
    private SyncFileTask locationTaskWriter;
    private SyncFileTask relationshipTaskWriter;
    private SyncFileTask socialGroupTaskWriter;
    private SyncFileTask visitTaskWriter;
    private SyncFileTask membershipTaskWriter;
    private SyncFileTask fieldWorkerTaskWriter;
    private SyncFileTask locationHierarchyTaskWriter;


    @Autowired
    public TaskExecutorImpl(AsyncTaskService asyncTaskService, FileResolver fileResolver) {
        this.asyncTaskService = asyncTaskService;
        this.fileResolver = fileResolver;
    }

    @Override
    public void executeMembershipTask() {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.MEMBERSHIP_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.MEMBERSHIP_TASK_NAME);
            File membershipXmlFile = fileResolver.resolveMembershipFile();
            membershipTaskWriter.run(new TaskContext(membershipXmlFile));
        }
    }

    @Override
    public void executeIndividualTask() {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.INDIVIDUAL_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.INDIVIDUAL_TASK_NAME);
            File individualXmlFile = fileResolver.resolveIndividualFile();
            individualTaskWriter.run(new TaskContext(individualXmlFile));
        }
    }

    @Override
    public void executeLocationTask() {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.LOCATION_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.LOCATION_TASK_NAME);
            File locationXmlFile = fileResolver.resolveLocationFile();
            locationTaskWriter.run(new TaskContext(locationXmlFile));
        }
    }

    @Override
    public void executeRelationshipTask() {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.RELATIONSHIP_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.RELATIONSHIP_TASK_NAME);
            File relationshipXmlFile = fileResolver.resolveRelationshipFile();
            relationshipTaskWriter.run(new TaskContext(relationshipXmlFile));
        }
    }

    @Override
    public void executeSocialGroupTask() {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.SOCIALGROUP_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.SOCIALGROUP_TASK_NAME);
            File socialGroupXmlFile = fileResolver.resolveSocialGroupFile();
            socialGroupTaskWriter.run(new TaskContext(socialGroupXmlFile));
        }
    }

    @Override
    public void executeVisitTask(int roundNumber) {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.VISIT_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.VISIT_TASK_NAME);
            File visitXmlFile = fileResolver.resolveVisitFile();
            TaskContext taskContext = new TaskContext(visitXmlFile);
            taskContext.addExtraData("roundNumber", roundNumber + "");
            visitTaskWriter.run(taskContext);
        }
    }

    @Override
    public void executeFieldWorkerTask() {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.FIELDWORKER_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.FIELDWORKER_TASK_NAME);
            File xmlFile = fileResolver.resolveFieldWorkerFile();
            fieldWorkerTaskWriter.run(new TaskContext(xmlFile));
        }
    }

    @Override
    public void executeLocationHierarchyTask() {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.LOCATIONHIERARCHY_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.LOCATIONHIERARCHY_TASK_NAME);
            File xmlFile = fileResolver.resolveLocationHierarchyFile();
            locationHierarchyTaskWriter.run(new TaskContext(xmlFile));
        }
    }

    @Resource(name="individualXmlWriter")
    public void setIndividualTaskWriter(SyncFileTask individualTaskWriter) {
        this.individualTaskWriter = individualTaskWriter;
    }

    @Resource(name="locationXmlWriter")
    public void setLocationTaskWriter(SyncFileTask individualTaskWriter) {
        this.locationTaskWriter = individualTaskWriter;
    }

    @Resource(name="relationshipXmlWriter")
    public void setRelationshipTaskWriter(SyncFileTask relationshipTaskWriter) {
        this.relationshipTaskWriter = relationshipTaskWriter;
    }

    @Resource(name="socialGroupXmlWriter")
    public void setSocialGroupTaskWriter(SyncFileTask socialGroupTaskWriter) {
        this.socialGroupTaskWriter = socialGroupTaskWriter;
    }

    @Resource(name="visitXmlWriter")
    public void setVisitTaskWriter(SyncFileTask visitTaskWriter) {
        this.visitTaskWriter = visitTaskWriter;
    }

    @Resource(name="membershipXmlWriter")
    public void setMembershipTaskWriter(SyncFileTask membershipTaskWriter) {
        this.membershipTaskWriter = membershipTaskWriter;
    }

    @Resource(name="fieldWorkerXmlWriter")
    public void setFieldWorkerTaskWriter(SyncFileTask fieldWorkerTaskWriter) {
        this.fieldWorkerTaskWriter = fieldWorkerTaskWriter;
    }

    @Resource(name="locationHierarchyXmlWriter")
    public void setLocationHierarchyTaskWriter(SyncFileTask locationHierarchyTaskWriter) {
        this.locationHierarchyTaskWriter = locationHierarchyTaskWriter;
    }
}
