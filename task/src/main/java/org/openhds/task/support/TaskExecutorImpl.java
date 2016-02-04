package org.openhds.task.support;

import org.openhds.task.TaskContext;
import org.openhds.task.XmlWriterTask;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

@Component("openhdsTaskExecutor")
public class TaskExecutorImpl implements TaskExecutor {

    private FileResolver fileResolver;
    private AsyncTaskService asyncTaskService;

    private XmlWriterTask individualTaskWriter;
    private XmlWriterTask locationTaskWriter;
    private XmlWriterTask relationshipTaskWriter;
    private XmlWriterTask socialGroupTaskWriter;
    private XmlWriterTask visitTaskWriter;
    private XmlWriterTask membershipTaskWriter;
    private XmlWriterTask fieldWorkerTaskWriter;
    private XmlWriterTask locationHierarchyTaskWriter;


    @Autowired
    public TaskExecutorImpl(AsyncTaskService asyncTaskService, FileResolver fileResolver) {
        this.asyncTaskService = asyncTaskService;
        this.fileResolver = fileResolver;
    }

    @Override
    public void executeMembershipXmlWriterTask() {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.MEMBERSHIP_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.MEMBERSHIP_TASK_NAME);
            File membershipXmlFile = fileResolver.resolveMembershipXmlFile();
            membershipTaskWriter.writeXmlAsync(new TaskContext(membershipXmlFile));
        }
    }

    @Override
    public void executeIndividualXmlWriterTask() {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.INDIVIDUAL_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.INDIVIDUAL_TASK_NAME);
            File individualXmlFile = fileResolver.resolveIndividualXmlFile();
            individualTaskWriter.writeXmlAsync(new TaskContext(individualXmlFile));
        }
    }

    @Override
    public void executeLocationXmlWriterTask() {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.LOCATION_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.LOCATION_TASK_NAME);
            File locationXmlFile = fileResolver.resolveLocationXmlFile();
            locationTaskWriter.writeXmlAsync(new TaskContext(locationXmlFile));
        }
    }

    @Override
    public void executeRelationshipXmlWriterTask() {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.RELATIONSHIP_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.RELATIONSHIP_TASK_NAME);
            File relationshipXmlFile = fileResolver.resolveRelationshipXmlFile();
            relationshipTaskWriter.writeXmlAsync(new TaskContext(relationshipXmlFile));
        }
    }

    @Override
    public void executeSocialGroupXmlWriterTask() {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.SOCIALGROUP_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.SOCIALGROUP_TASK_NAME);
            File socialGroupXmlFile = fileResolver.resolveSocialGroupXmlFile();
            socialGroupTaskWriter.writeXmlAsync(new TaskContext(socialGroupXmlFile));
        }
    }

    @Override
    public void executeVisitWriterTask(int roundNumber) {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.VISIT_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.VISIT_TASK_NAME);
            File visitXmlFile = fileResolver.resolveVisitXmlFile();
            TaskContext taskContext = new TaskContext(visitXmlFile);
            taskContext.addExtraData("roundNumber", roundNumber + "");
            visitTaskWriter.writeXmlAsync(taskContext);
        }
    }

    @Override
    public void executeFieldWorkerXmlWriterTask() {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.FIELDWORKER_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.FIELDWORKER_TASK_NAME);
            File xmlFile = fileResolver.resolveFieldWorkerFile();
            fieldWorkerTaskWriter.writeXmlAsync(new TaskContext(xmlFile));
        }
    }

    @Override
    public void executeLocationHierarchyXmlWriterTask() {
        if (asyncTaskService.taskShouldRun(AsyncTaskService.LOCATIONHIERARCHY_TASK_NAME)) {
            asyncTaskService.startTask(AsyncTaskService.LOCATIONHIERARCHY_TASK_NAME);
            File xmlFile = fileResolver.resolveLocationHierarchyFile();
            locationHierarchyTaskWriter.writeXmlAsync(new TaskContext(xmlFile));
        }
    }

    @Resource(name="individualXmlWriter")
    public void setIndividualTaskWriter(XmlWriterTask individualTaskWriter) {
        this.individualTaskWriter = individualTaskWriter;
    }

    @Resource(name="locationXmlWriter")
    public void setLocationTaskWriter(XmlWriterTask individualTaskWriter) {
        this.locationTaskWriter = individualTaskWriter;
    }

    @Resource(name="relationshipXmlWriter")
    public void setRelationshipTaskWriter(XmlWriterTask relationshipTaskWriter) {
        this.relationshipTaskWriter = relationshipTaskWriter;
    }

    @Resource(name="socialGroupXmlWriter")
    public void setSocialGroupTaskWriter(XmlWriterTask socialGroupTaskWriter) {
        this.socialGroupTaskWriter = socialGroupTaskWriter;
    }

    @Resource(name="visitXmlWriter")
    public void setVisitTaskWriter(XmlWriterTask visitTaskWriter) {
        this.visitTaskWriter = visitTaskWriter;
    }

    @Resource(name="membershipXmlWriter")
    public void setMembershipTaskWriter(XmlWriterTask membershipTaskWriter) {
        this.membershipTaskWriter = membershipTaskWriter;
    }

    @Resource(name="fieldWorkerXmlWriter")
    public void setFieldWorkerTaskWriter(XmlWriterTask fieldWorkerTaskWriter) {
        this.fieldWorkerTaskWriter = fieldWorkerTaskWriter;
    }

    @Resource(name="locationHierarchyXmlWriter")
    public void setLocationHierarchyTaskWriter(XmlWriterTask locationHierarchyTaskWriter) {
        this.locationHierarchyTaskWriter = locationHierarchyTaskWriter;
    }
}
