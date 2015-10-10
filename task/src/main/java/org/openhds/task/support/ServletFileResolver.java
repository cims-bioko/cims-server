package org.openhds.task.support;

import org.openhds.task.service.AsyncTaskService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

@Component
public class ServletFileResolver implements FileResolver, ServletContextAware {

    private ServletContext servletContext;
    private Map<String, String> asyncFiles;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @PostConstruct
    public void createAsyncMap() {
        asyncFiles = new HashMap<>();
        asyncFiles.put(AsyncTaskService.INDIVIDUAL_TASK_NAME, "individual.xml");
        asyncFiles.put(AsyncTaskService.LOCATION_TASK_NAME, "location.xml");
        asyncFiles.put(AsyncTaskService.MEMBERSHIP_TASK_NAME, "membership.xml");
        asyncFiles.put(AsyncTaskService.RELATIONSHIP_TASK_NAME, "relationship.xml");
        asyncFiles.put(AsyncTaskService.RESIDENCY_TASK_NAME, "residency.xml");
        asyncFiles.put(AsyncTaskService.SOCIALGROUP_TASK_NAME, "socialgroup.xml");
        asyncFiles.put(AsyncTaskService.VISIT_TASK_NAME, "visit.xml");
    }

    protected File getGeneratedXmlFolder() {
        String fullPath = servletContext.getRealPath("/");
        File generatedXmlFileDir = new File(fullPath + File.separator + "generated-xml");
        generatedXmlFileDir.mkdirs();
        return generatedXmlFileDir;
    }

    @Override
    public File resolveResidencyXmlFile() {
        return buildFile(AsyncTaskService.RESIDENCY_TASK_NAME);
    }

    @Override
    public File resolveMembershipXmlFile() {
        return buildFile(AsyncTaskService.MEMBERSHIP_TASK_NAME);
    }

    @Override
    public File resolveIndividualXmlFile() {
        return buildFile(AsyncTaskService.INDIVIDUAL_TASK_NAME);
    }

    @Override
    public File resolveLocationXmlFile() {
        return buildFile(AsyncTaskService.LOCATION_TASK_NAME);
    }

    @Override
    public File resolveRelationshipXmlFile() {
        return buildFile(AsyncTaskService.RELATIONSHIP_TASK_NAME);
    }

    @Override
    public File resolveSocialGroupXmlFile() {
        return buildFile(AsyncTaskService.SOCIALGROUP_TASK_NAME);
    }

    @Override
    public File resolveVisitXmlFile() {
        return buildFile(AsyncTaskService.VISIT_TASK_NAME);
    }

    @Override
    public File getFileForTask(String taskName) {
        return buildFile(asyncFiles.get(taskName));
    }

    protected File buildFile(String file) {
        return new File(getGeneratedXmlFolder(), file);
    }

}
