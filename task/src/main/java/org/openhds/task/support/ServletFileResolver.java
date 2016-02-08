package org.openhds.task.support;

import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import static org.openhds.task.service.AsyncTaskService.MOBILEDB_TASK_NAME;

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
        asyncFiles.put(MOBILEDB_TASK_NAME, "cims-tablet.db");
    }

    @Override
    public File resolveMobileDBFile() {
        return getFileForTask(MOBILEDB_TASK_NAME);
    }

    protected File getGeneratedXmlFolder() {
        String fullPath = servletContext.getRealPath("/WEB-INF");
        File generatedXmlFileDir = new File(fullPath + File.separator + "cached-files");
        generatedXmlFileDir.mkdirs();
        return generatedXmlFileDir;
    }

    @Override
    public File getFileForTask(String taskName) {
        return new File(getGeneratedXmlFolder(), asyncFiles.get(taskName));
    }
}
