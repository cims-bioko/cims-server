package org.openhds.task.support;

import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import static org.openhds.task.service.AsyncTaskService.INDIVIDUAL_TASK_NAME;
import static org.openhds.task.service.AsyncTaskService.LOCATION_TASK_NAME;
import static org.openhds.task.service.AsyncTaskService.MEMBERSHIP_TASK_NAME;
import static org.openhds.task.service.AsyncTaskService.RELATIONSHIP_TASK_NAME;
import static org.openhds.task.service.AsyncTaskService.SOCIALGROUP_TASK_NAME;
import static org.openhds.task.service.AsyncTaskService.VISIT_TASK_NAME;

@Component
public class ServletFileResolver implements FileResolver, ServletContextAware {

    private ServletContext servletContext;
    private Map<String, String> asyncFiles;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @PostConstruct
    public void createTaskFileMap() {
        asyncFiles = new HashMap<>();
        asyncFiles.put(INDIVIDUAL_TASK_NAME, "individual.xml");
        asyncFiles.put(LOCATION_TASK_NAME, "location.xml");
        asyncFiles.put(MEMBERSHIP_TASK_NAME, "membership.xml");
        asyncFiles.put(RELATIONSHIP_TASK_NAME, "relationship.xml");
        asyncFiles.put(SOCIALGROUP_TASK_NAME, "socialgroup.xml");
        asyncFiles.put(VISIT_TASK_NAME, "visit.xml");
    }

    @Override
    public File resolveMembershipXmlFile() {
        return getFileForTask(MEMBERSHIP_TASK_NAME);
    }

    @Override
    public File resolveIndividualXmlFile() {
        return getFileForTask(INDIVIDUAL_TASK_NAME);
    }

    @Override
    public File resolveLocationXmlFile() {
        return getFileForTask(LOCATION_TASK_NAME);
    }

    @Override
    public File resolveRelationshipXmlFile() {
        return getFileForTask(RELATIONSHIP_TASK_NAME);
    }

    @Override
    public File resolveSocialGroupXmlFile() {
        return getFileForTask(SOCIALGROUP_TASK_NAME);
    }

    @Override
    public File resolveVisitXmlFile() {
        return getFileForTask(VISIT_TASK_NAME);
    }

    protected File getGeneratedXmlFolder() {
        String fullPath = servletContext.getRealPath("/");
        File generatedXmlFileDir = new File(fullPath + File.separator + "generated-xml");
        generatedXmlFileDir.mkdirs();
        return generatedXmlFileDir;
    }

    @Override
    public File getFileForTask(String taskName) {
        return new File(getGeneratedXmlFolder(), asyncFiles.get(taskName));
    }
}
