package org.openhds.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppVersion implements ServletContextAware {

    private static final Logger log = LoggerFactory.getLogger(AppVersion.class);

    public static final String VERSION_KEY = "version";

    private String propsPath;
    private Properties archiveProps;

    public AppVersion(String propertiesPath) {
        propsPath = propertiesPath;
    }

    public String getVersionNumber() {
        if (archiveProps != null && archiveProps.containsKey(VERSION_KEY)) {
            return archiveProps.getProperty(VERSION_KEY);
        } else {
            return "Unknown";
        }
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        InputStream propStream = servletContext.getResourceAsStream(propsPath);
        if (propStream != null) {
            try {
                archiveProps = new Properties();
                archiveProps.load(propStream);
            } catch (IOException e) {
                log.warn("failed to load build properties from path {}", propsPath, e);
            }
        } else {
            log.warn("failed to find version properties from path {}", propsPath);
        }
    }
}